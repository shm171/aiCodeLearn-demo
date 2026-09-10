package com.mylab.ailearn.core.model.commonmodel;

import java.util.List;

/**
 * 双层批改后的最终结果：规则校验 + LLM 深度批改合并、去重、计分。
 */
public record GradingResult(
        List<GradedError> errors,
        int score,
        String feedback) {

    public GradingResult {
        errors = errors == null ? List.of() : List.copyOf(errors);
    }
}