package com.mylab.ailearn.core.service.ai;

import com.mylab.ailearn.core.enums.ErrorCategory;
import com.mylab.ailearn.core.enums.RuleType;
import com.mylab.ailearn.core.model.commonmodel.GradedError;
import com.mylab.ailearn.core.model.commonmodel.LlmReview;
import com.mylab.ailearn.core.model.commonmodel.LlmReviewStatus;
import com.mylab.ailearn.core.model.commonmodel.SourceFile;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * LLM 批改结果的落地校验。
 *
 * <p>模型输出不是可信证据：结构合法（能反序列化）不代表语义合法。本校验在结果进入
 * 分数、错题归档与报告之前，按本次提交的<b>权威源码</b>检查分类、字段长度、
 * 行号范围、数量预算，以及错误代码与分类是否自洽。</p>
 *
 * <p>处理策略：</p>
 * <ul>
 *   <li>单条 issue 任一项不通过 → 丢弃该条，其余保留，结论标记为 {@link LlmReviewStatus#PARTIAL}；</li>
 *   <li>issue 总数超过 {@link #MAX_ISSUES} → 整份结论作废，标记为 {@link LlmReviewStatus#UNAVAILABLE}；</li>
 *   <li>全部条目被丢弃 → 标记为 {@link LlmReviewStatus#UNAVAILABLE}，调用方不得当作「没有发现问题」。</li>
 * </ul>
 */
@Slf4j
@Service
public class LlmReviewValidator {

    /** 单次模型输出允许的 issue 条数上限。 */
    public static final int MAX_ISSUES = 50;
    /** 单条 issue 允许的行号数量上限。 */
    public static final int MAX_LINES_PER_ISSUE = 50;
    /** errorType 长度上限。 */
    public static final int MAX_ERROR_TYPE_LENGTH = 60;
    /** errorCode 长度上限。 */
    public static final int MAX_ERROR_CODE_LENGTH = 60;
    /** message 长度上限。 */
    public static final int MAX_MESSAGE_LENGTH = 500;
    /** fixSuggestion 长度上限。 */
    public static final int MAX_SUGGESTION_LENGTH = 1000;
    /** summary 长度上限，超出即截断。 */
    public static final int MAX_SUMMARY_LENGTH = 500;

    /** 已知规则代码 → 该代码应有的错误分类，用于校验分类与代码是否自洽。 */
    private static final Map<String, ErrorCategory> RULE_TYPE_CATEGORIES = Arrays.stream(RuleType.values())
            .collect(Collectors.toUnmodifiableMap(RuleType::code, RuleType::category, (first, second) -> first));

    /**
     * 校验一份模型输出。
     *
     * @param raw    模型返回的原始结论，可以为 null（视为不可采信）
     * @param source 本次提交的权威源码，用于限定行号范围
     * @return 已按校验结果收敛的结论，status 由服务端判定，永不返回 null
     */
    public LlmReview validate(LlmReview raw, SourceFile source) {
        if (raw == null) {
            log.warn("LLM 未返回可解析的批改结果");
            return LlmReview.unavailable("LLM 未返回可解析的批改结果。");
        }

        String summary = truncate(raw.summary(), MAX_SUMMARY_LENGTH);
        List<GradedError> declared = raw.issues();

        if (declared.isEmpty()) {
            return new LlmReview(List.of(), summary, LlmReviewStatus.COMPLETED);
        }
        if (declared.size() > MAX_ISSUES) {
            log.warn("LLM 输出 issue 数量 {} 超过上限 {}，整份结论作废", declared.size(), MAX_ISSUES);
            return new LlmReview(List.of(), summary, LlmReviewStatus.UNAVAILABLE);
        }

        int lineCount = lineCount(source == null ? null : source.content());
        List<GradedError> accepted = new ArrayList<>(declared.size());
        int rejected = 0;
        for (GradedError issue : declared) {
            String problem = problemOf(issue, lineCount);
            if (problem == null) {
                accepted.add(issue);
            } else {
                rejected++;
                log.warn("丢弃不可信的 LLM 批改条目（{}）：errorCode={}", problem,
                        issue == null ? null : issue.errorCode());
            }
        }

        if (accepted.isEmpty()) {
            log.warn("LLM 输出的 {} 条批改条目全部未通过校验，结论不可采信", declared.size());
            return new LlmReview(List.of(), summary, LlmReviewStatus.UNAVAILABLE);
        }
        return new LlmReview(accepted, summary,
                rejected > 0 ? LlmReviewStatus.PARTIAL : LlmReviewStatus.COMPLETED);
    }

    /** 返回该条 issue 不通过的原因；通过时返回 null。 */
    private String problemOf(GradedError issue, int lineCount) {
        if (issue == null) {
            return "条目为空";
        }
        if (issue.category() == null) {
            return "缺少错误分类";
        }
        if (isBlank(issue.errorType())) {
            return "缺少错误类型";
        }
        if (issue.errorType().length() > MAX_ERROR_TYPE_LENGTH) {
            return "错误类型超长";
        }
        if (isBlank(issue.errorCode())) {
            return "缺少错误代码";
        }
        if (issue.errorCode().length() > MAX_ERROR_CODE_LENGTH) {
            return "错误代码超长";
        }
        if (issue.message() != null && issue.message().length() > MAX_MESSAGE_LENGTH) {
            return "错误说明超长";
        }
        if (issue.fixSuggestion() != null && issue.fixSuggestion().length() > MAX_SUGGESTION_LENGTH) {
            return "修改建议超长";
        }
        // 业务关系：已知规则代码必须与声明的分类一致，避免用错分类降低扣分权重
        ErrorCategory expected = RULE_TYPE_CATEGORIES.get(issue.errorCode());
        if (expected != null && expected != issue.category()) {
            return "错误分类与错误代码不一致";
        }

        List<Integer> lines = issue.line();
        if (lines.size() > MAX_LINES_PER_ISSUE) {
            return "行号数量超限";
        }
        for (Integer line : lines) {
            if (line == null || line < 1 || line > lineCount) {
                return "行号越界";
            }
        }
        return null;
    }

    /** 源码总行数；空内容按 1 行处理，保证任何行号都会被判定为空集。 */
    private int lineCount(String content) {
        if (content == null || content.isEmpty()) {
            return 1;
        }
        int lines = 1;
        for (int i = 0; i < content.length(); i++) {
            if (content.charAt(i) == '\n') {
                lines++;
            }
        }
        return lines;
    }

    private String truncate(String value, int maxLength) {
        if (value == null || value.length() <= maxLength) {
            return value;
        }
        return value.substring(0, maxLength);
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
