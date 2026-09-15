package com.mylab.ailearn.core.model.commonmodel;

import com.mylab.ailearn.core.enums.ErrorCategory;

import java.util.List;

/**
 * 一次批改发现的某一条问题。
 *
 * <p>规则静态检查与 LLM 深度批改产生的条目都收敛成这个形态，再由 {@code source}
 * 区分来源。落库时会被转换成 {@link ErrorRecord}。</p>
 *
 * @param category      错误大类，取自 {@link ErrorCategory}；<b>不应为 null</b>，
 *                      分类缺失的条目在批改阶段就会被丢弃
 * @param errorType     错误类型名，例如"数组越界访问"
 * @param errorCode     错误代码，例如 ARRAY_OUT_OF_BOUNDS
 * @param message       面向学生的错误说明
 * @param fixSuggestion 修改建议，可为 null
 * @param source        来源标识：{@code GradingServiceImpl.SOURCE_RULE}（规则静态检查）
 *                      或 {@code GradingServiceImpl.SOURCE_LLM}（LLM 深度批改）
 * @param line          出错行号列表，行号从 1 开始；无法定位时为空列表
 */
public record GradedError(
        ErrorCategory category,
        String errorType,
        String errorCode,
        String message,
        String fixSuggestion,
        String source,
        List<Integer> line) {

    public GradedError {
        line = line == null ? List.of() : line.stream().distinct().sorted().toList();
    }
}
