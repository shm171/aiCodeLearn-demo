package com.mylab.ailearn.core.service;

import com.mylab.ailearn.core.enums.ErrorCategory;
import com.mylab.ailearn.core.model.commonmodel.CompileCheckReport;
import com.mylab.ailearn.core.model.commonmodel.CompileError;
import com.mylab.ailearn.core.model.commonmodel.CompileStatus;
import com.mylab.ailearn.core.model.commonmodel.GradedError;
import com.mylab.ailearn.core.model.commonmodel.GradingResult;
import com.mylab.ailearn.core.model.commonmodel.LlmReview;
import com.mylab.ailearn.core.model.commonmodel.RuleViolation;
import com.mylab.ailearn.core.model.commonmodel.SourceFile;
import com.mylab.ailearn.core.model.commonmodel.StaticCheckReport;
import com.mylab.ailearn.core.service.spi.LlmGradingClient;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 双层批改的默认实现：先规则静态筛查，再 LLM 深度批改，最后合并、去重并计分。
 */
@Service
@RequiredArgsConstructor
public class GradingServiceImpl implements GradingService {

    public static final String SOURCE_RULE = "规则校验";
    public static final String SOURCE_LLM = "LLM 深度批改";

    private final StaticCheckService staticCheckService;
    private final CompilationCheckService compilationCheckService;
    private final ObjectProvider<LlmGradingClient> llmGradingClientProvider;

    // 暴露接口，编排业务
    @Override
    public GradingResult grade(SourceFile sourceFile) {
        requireSource(sourceFile);

        StaticCheckReport staticReport = staticCheckService.check(sourceFile);
        LlmReview review = llmReview(sourceFile);
        CompileCheckReport compileReport = compilationCheckService.check(sourceFile);

        List<GradedError> errors = new ArrayList<>();
        errors.addAll(staticCheckErrors(staticReport));
        errors.addAll(llmCheckErrors(review));
        errors.addAll(compilationCheckErrors(compileReport));
        errors = dedupe(errors);

        int score = calculateScore(errors, compileReport);
        String feedback = selectFeedback(errors, review, compileReport);

        return new GradingResult(errors, score, feedback);
    }


    // 源码校验
    private void requireSource(SourceFile sourceFile) {
        if (sourceFile == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "sourceFile 不能为空");
        }
    }

    // 调 LLM；没有 LlmGradingClient bean 时返回 null
    private LlmReview llmReview(SourceFile sourceFile) {
        LlmGradingClient llmClient = llmGradingClientProvider.getIfAvailable();
        return llmClient == null ? null : llmClient.review(sourceFile);
    }

    // 规则静态检查 → 错误
    private List<GradedError> staticCheckErrors(StaticCheckReport staticReport) {
        List<GradedError> errors = new ArrayList<>();
        for (RuleViolation violation : staticReport.violations()) {
            errors.add(new GradedError(
                    violation.category(),
                    violation.ruleType().label(),
                    violation.ruleType().code(),
                    violation.message(),
                    violation.suggestion(),
                    SOURCE_RULE,
                    violation.line() == null ? List.of() : List.of(violation.line())));
        }
        return errors;
    }

    // LLM 批改 → 错误
    private List<GradedError> llmCheckErrors(LlmReview review) {
        if (review == null || review.issues() == null) {
            return List.of();
        }
        List<GradedError> errors = new ArrayList<>();
        for (GradedError issue : review.issues()) {
            errors.add(new GradedError(
                    issue.category(),
                    issue.errorType(),
                    issue.errorCode(),
                    issue.message() == null ? issue.errorType() : issue.message(),
                    issue.fixSuggestion(),
                    SOURCE_LLM,
                    issue.line() == null ? List.of() : issue.line()));
        }
        return errors;
    }

    // 编译检查 → 错误
    private List<GradedError> compilationCheckErrors(CompileCheckReport compileReport) {
        List<GradedError> errors = new ArrayList<>();
        if (compileReport.status() == CompileStatus.FAILED) {
            if (compileReport.errors().isEmpty()) {
                // 编译失败但没有具体错误明细，补一条通用错误
                errors.add(new GradedError(
                        ErrorCategory.SYNTAX_ERROR, "编译错误", "COMPILE_ERROR",
                        "编译未通过", null, SOURCE_RULE, List.of()));
            } else {
                for (CompileError compileError : compileReport.errors()) {
                    errors.add(new GradedError(
                            ErrorCategory.SYNTAX_ERROR, "编译错误", "COMPILE_ERROR",
                            compileError.message(), compileError.suggestion(),
                            SOURCE_RULE,
                            compileError.line() > 0 ? List.of(compileError.line()) : List.of()));
                }
            }
        }
        return errors;
    }

    // 错误清单去重
    private List<GradedError> dedupe(List<GradedError> errors) {
        Map<String, GradedError> dedup = new LinkedHashMap<>();
        for (GradedError error : errors) {
            String key = (error.category() == null ? "" : error.category().name())
                    + "|" + (error.errorCode() == null ? "" : error.errorCode())
                    + "|" + (error.errorType() == null ? "" : error.errorType());
            dedup.putIfAbsent(key, error);
        }
        return new ArrayList<>(dedup.values());
    }

    // 计分
    private int calculateScore(List<GradedError> errors, CompileCheckReport compileReport) {
        int score = 100;
        for (GradedError error : errors) {
            score -= deduction(error.category());
        }
        score = Math.max(0, score);
        if (compileReport.status() == CompileStatus.FAILED) {
            score = Math.min(score, 60); // 编译失败时封顶 60 分
        }
        return score;
    }

    // 错误类型的扣分权重
    private int deduction(ErrorCategory category) {
        return category == null ? 0 : category.deductionWeight();
    }

    // 选择反馈：LLM 有结论用原话，否则用统计文案
    private String selectFeedback(List<GradedError> errors, LlmReview review, CompileCheckReport compileReport) {
        boolean llmAvailable = review != null;
        String llmSummary = review == null ? null : review.summary();
        if (llmSummary != null && !llmSummary.isBlank()) {
            return llmSummary;
        }
        return buildFeedback(errors, llmAvailable, compileReport.status());
    }

    // 生成非 LLM 的统计文案
    private String buildFeedback(List<GradedError> errors, boolean llmAvailable, CompileStatus compileStatus) {
        long format = errors.stream().filter(e -> e.category() == ErrorCategory.FORMAT_ERROR).count();
        long syntax = errors.stream().filter(e -> e.category() == ErrorCategory.SYNTAX_ERROR).count();
        long logic = errors.stream().filter(e -> e.category() == ErrorCategory.LOGIC_ERROR).count();
        long uncategorized = errors.stream().filter(e -> e.category() == null).count();

        StringBuilder feedback = new StringBuilder();
        feedback.append("共发现 ").append(errors.size()).append(" 处问题");
        feedback.append("：格式错误 ").append(format)
                .append("，语法错误 ").append(syntax)
                .append("，逻辑错误 ").append(logic);
        if (uncategorized > 0) {
            feedback.append("，未分类错误 ").append(uncategorized);
        }
        feedback.append(llmAvailable ? "；已结合 LLM 深度批改。" : "；LLM 深度批改未接入，本次仅规则校验。");
        feedback.append(switch (compileStatus) {
            case PASSED -> "；源码编译通过。";
            case FAILED -> "；源码编译未通过。";
            case SKIPPED -> "；编译检查未执行。";
        });
        return feedback.toString();
    }
}
