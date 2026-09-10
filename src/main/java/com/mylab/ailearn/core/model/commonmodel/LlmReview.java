package com.mylab.ailearn.core.model.commonmodel;

import java.util.List;

/**
 * LLM 深度批改结果，主要补充规则筛不出的逻辑错误与算法思路问题。
 */
public record LlmReview(
        List<GradedError> issues,
        String summary) {

    public LlmReview {
        issues = issues == null ? List.of() : List.copyOf(issues);
    }
}