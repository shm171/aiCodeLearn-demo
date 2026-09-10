package com.mylab.ailearn.core.model.commonmodel;

import java.util.List;

/**
 * LLM 深度批改结果，主要补充规则筛不出的逻辑错误与算法思路问题。
 *
 * <p>{@code status} 描述这份结论的可信程度：模型返回的原始内容必须经过
 * {@code LlmReviewValidator} 校验，只有 COMPLETED 才代表结论完整；
 * PARTIAL 表示非法条目已丢弃、结论不完整；UNAVAILABLE 表示模型未接入、
 * 调用失败或输出整体不可采信。<b>调用方不得把 UNAVAILABLE 当成「没有发现问题」。</b></p>
 *
 * <p>{@code status} 一律由服务端校验得出，不从模型输出里读取。</p>
 */
public record LlmReview(
        List<GradedError> issues,
        String summary,
        LlmReviewStatus status) {

    public LlmReview {
        issues = issues == null ? List.of() : List.copyOf(issues);
        // 状态缺失时按「不可采信」处理，避免出现默认可信的漏洞
        status = status == null ? LlmReviewStatus.UNAVAILABLE : status;
    }

    /** 模型未接入、调用失败或缺少源码时的降级结果：无 issues，且明确标记为不可采信。 */
    public static LlmReview unavailable(String summary) {
        return new LlmReview(List.of(), summary, LlmReviewStatus.UNAVAILABLE);
    }
}
