package com.mylab.ailearn.core.model.commonmodel;

import com.mylab.ailearn.core.enums.ErrorCategory;

import java.util.List;

/**
 * 一条批改出的错误。source 用于区分是规则校验还是 LLM 深度批改产生。
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
