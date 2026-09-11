package com.mylab.ailearn.core.service;

import com.mylab.ailearn.core.enums.ErrorCategory;
import com.mylab.ailearn.core.model.commonmodel.GradedError;
import com.mylab.ailearn.core.model.commonmodel.GradingResult;
import com.mylab.ailearn.core.model.commonmodel.GradingStage;
import com.mylab.ailearn.core.model.commonmodel.GradingStageOutcome;
import com.mylab.ailearn.core.model.commonmodel.GradingStageReport;
import com.mylab.ailearn.core.model.commonmodel.GradingStatus;
import com.mylab.ailearn.core.model.commonmodel.LlmReview;
import com.mylab.ailearn.core.model.commonmodel.LlmReviewStatus;
import com.mylab.ailearn.core.model.commonmodel.RuleViolation;
import com.mylab.ailearn.core.model.commonmodel.SourceFile;
import com.mylab.ailearn.core.model.commonmodel.StaticCheckReport;
import com.mylab.ailearn.core.service.ai.LlmReviewValidator;
import com.mylab.ailearn.core.service.spi.LlmGradingClient;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 双层批改的默认实现：先规则静态检查，再 LLM 深度批改，最后合并、去重并计分。
 *
 * <p>模型输出在本层统一校验：只有通过 {@link LlmReviewValidator} 的条目才会进入
 * 分数与错题归档，未通过的部分体现在反馈文案里，不会被当成「没有发现问题」。</p>
 *
 * <p>结果同时带上各阶段状态与整体完成度：没有检测结果不等于检查通过，
 * 只有全部阶段完整完成时 {@code GradingResult#scoreAdoptable()} 才为 true。</p>
 */
@Service
@RequiredArgsConstructor
public class GradingServiceImpl implements GradingService {

    /** 规则静态检查产出的问题在结果中的来源标识（会展示给学生，文案保持不变）。 */
    public static final String SOURCE_RULE = "规则校验";
    /** LLM 深度批改产出的问题在结果中的来源标识。 */
    public static final String SOURCE_LLM = "LLM 深度批改";

    /** 分类缺失时的兜底扣分权重：按最高权重扣，避免无法归类的问题「免罚」。 */
    private static final int UNCLASSIFIED_DEDUCTION = 10;

    private final StaticCheckService staticCheckService;
    private final ObjectProvider<LlmGradingClient> llmGradingClientProvider;
    private final LlmReviewValidator llmReviewValidator;

    // 暴露接口，编排业务
    @Override
    public GradingResult grade(SourceFile sourceFile) {
        requireSource(sourceFile);

        StaticCheckReport staticReport = staticCheckService.check(sourceFile);
        LlmReview review = llmReview(sourceFile);

        List<GradedError> errors = new ArrayList<>();
        errors.addAll(staticCheckErrors(staticReport));
        errors.addAll(llmCheckErrors(review));
        errors = dedupe(errors);

        // 各阶段状态结构化输出：调用方无需从反馈文案里猜分数是否可信
        List<GradingStageReport> stages = List.of(
                ruleStageReport(staticReport),
                llmStageReport(review));

        int score = calculateScore(errors);
        String feedback = selectFeedback(errors, review);

        return new GradingResult(errors, score, feedback, overallStatus(stages), stages);
    }

    // 规则静态检查阶段状态：该阶段总会执行
    private GradingStageReport ruleStageReport(StaticCheckReport staticReport) {
        return new GradingStageReport(GradingStage.RULE_STATIC_CHECK, GradingStageOutcome.COMPLETED,
                "规则静态检查已完成，共发现 " + staticReport.errorCount() + " 处违规");
    }

    // LLM 阶段状态：未接入 / 完整 / 部分 / 不可采信 各自对应不同状态
    private GradingStageReport llmStageReport(LlmReview review) {
        if (review == null) {
            return new GradingStageReport(GradingStage.LLM_REVIEW, GradingStageOutcome.NOT_CONFIGURED,
                    "未接入 LLM 深度批改客户端，本次结论仅来自规则静态检查");
        }
        return switch (review.status()) {
            case COMPLETED -> new GradingStageReport(GradingStage.LLM_REVIEW, GradingStageOutcome.COMPLETED,
                    "LLM 深度批改已完成");
            case PARTIAL -> new GradingStageReport(GradingStage.LLM_REVIEW, GradingStageOutcome.PARTIAL,
                    "LLM 输出部分未通过校验已被丢弃，结论不完整");
            case UNAVAILABLE -> new GradingStageReport(GradingStage.LLM_REVIEW, GradingStageOutcome.UNAVAILABLE,
                    "LLM 深度批改未产出可信结论");
            case UNVERIFIED -> new GradingStageReport(GradingStage.LLM_REVIEW, GradingStageOutcome.UNAVAILABLE,
                    "LLM 结论未经校验，按不可采信处理");
        };
    }

    // 整体完成度：不可采信优先，其次部分完成/未启用，只有全部完整完成才是 COMPLETED
    private GradingStatus overallStatus(List<GradingStageReport> stages) {
        boolean incomplete = false;
        for (GradingStageReport stage : stages) {
            GradingStageOutcome outcome = stage.outcome();
            if (outcome == GradingStageOutcome.UNAVAILABLE) {
                return GradingStatus.UNAVAILABLE;
            }
            if (outcome != GradingStageOutcome.COMPLETED) {
                incomplete = true;
            }
        }
        return incomplete ? GradingStatus.PARTIAL : GradingStatus.COMPLETED;
    }


    // 源码校验：非空 + 与文件解析入口同一套输入预算（防御直接构造 SourceFile 的调用方）
    private void requireSource(SourceFile sourceFile) {
        if (sourceFile == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "sourceFile 不能为空");
        }
        SourceInputValidator.requireWithinBudget(sourceFile.filename(), sourceFile.content());
    }

    // 调 LLM；没有 LlmGradingClient bean 时返回 null；模型输出一律先校验再使用
    private LlmReview llmReview(SourceFile sourceFile) {
        LlmGradingClient llmClient = llmGradingClientProvider.getIfAvailable();
        if (llmClient == null) {
            return null;
        }
        return llmReviewValidator.validate(llmClient.review(sourceFile), sourceFile);
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
            if (issue.category() == null) {
                // 无分类的问题既不能归因也不能计分，防御性丢弃（校验层本应已拦截）
                continue;
            }
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

    /**
     * 错误清单去重：同一「分类 + 错误代码 + 错误类型」只保留一条。
     *
     * <p>保留先出现的那条（规则静态检查排在 LLM 之前），但会<b>合并行号</b>：
     * 同一类错误出现在多个位置时，合并后学生才能看到全部出错位置，
     * 而不是只剩最先出现的那一处。</p>
     */
    private List<GradedError> dedupe(List<GradedError> errors) {
        Map<String, GradedError> dedup = new LinkedHashMap<>();
        for (GradedError error : errors) {
            String key = (error.category() == null ? "" : error.category().name())
                    + "|" + (error.errorCode() == null ? "" : error.errorCode())
                    + "|" + (error.errorType() == null ? "" : error.errorType());
            dedup.merge(key, error, GradingServiceImpl::mergeLines);
        }
        return new ArrayList<>(dedup.values());
    }

    /** 同一错误类型重复出现时，保留先出现的条目并把行号并起来；没有新行号则复用原对象。 */
    private static GradedError mergeLines(GradedError kept, GradedError duplicate) {
        Set<Integer> lines = new LinkedHashSet<>(kept.line());
        lines.addAll(duplicate.line());
        if (lines.size() == kept.line().size()) {
            return kept;
        }
        return new GradedError(kept.category(), kept.errorType(), kept.errorCode(),
                kept.message(), kept.fixSuggestion(), kept.source(), List.copyOf(lines));
    }

    // 计分
    private int calculateScore(List<GradedError> errors) {
        int score = 100;
        for (GradedError error : errors) {
            score -= deduction(error.category());
        }
        return Math.max(0, score);
    }

    // 错误类型的扣分权重；分类缺失时按最高权重扣分
    private int deduction(ErrorCategory category) {
        return category == null ? UNCLASSIFIED_DEDUCTION : category.deductionWeight();
    }

    // 选择反馈：LLM 有结论用原话（不完整时附加提示），否则用统计文案
    private String selectFeedback(List<GradedError> errors, LlmReview review) {
        LlmReviewStatus status = review == null ? null : review.status();
        String llmSummary = review == null ? null : review.summary();
        if (llmSummary != null && !llmSummary.isBlank()) {
            String warning = incompleteWarning(status);
            return warning == null ? llmSummary : llmSummary + warning;
        }
        return buildFeedback(errors, status);
    }

    // 结论不完整时的补充说明；结论完整或未接入模型时返回 null
    private String incompleteWarning(LlmReviewStatus status) {
        if (status == LlmReviewStatus.PARTIAL) {
            return "（注意：LLM 批改结果不完整，部分输出未通过校验已被丢弃）";
        }
        if (status == LlmReviewStatus.UNAVAILABLE || status == LlmReviewStatus.UNVERIFIED) {
            return "（注意：LLM 批改未产出可信结论，本次仅规则校验）";
        }
        return null;
    }

    // 生成非 LLM 的统计文案
    private String buildFeedback(List<GradedError> errors, LlmReviewStatus llmStatus) {
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
        feedback.append(llmStatusNote(llmStatus));
        return feedback.toString();
    }

    // LLM 阶段的结论状态说明；null 表示未接入 LLM 客户端
    private String llmStatusNote(LlmReviewStatus status) {
        if (status == null) {
            return "；LLM 深度批改未接入，本次仅规则校验。";
        }
        return switch (status) {
            case COMPLETED -> "；已结合 LLM 深度批改。";
            case PARTIAL -> "；LLM 深度批改结果不完整（部分输出未通过校验），本次结论仅供参考。";
            case UNAVAILABLE -> "；LLM 深度批改未产出可信结论，本次仅规则校验。";
            case UNVERIFIED -> "；LLM 深度批改结论未经校验，按不可采信处理。";
        };
    }
}
