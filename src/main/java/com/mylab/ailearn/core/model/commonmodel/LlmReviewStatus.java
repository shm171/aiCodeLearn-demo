package com.mylab.ailearn.core.model.commonmodel;

/**
 * LLM 深度批改结论的可信状态。
 *
 * <p>模型输出不是可信证据：只有通过 {@code LlmReviewValidator} 校验的条目才会带
 * {@link #COMPLETED}；模型未接入、调用失败或输出整体不可采信时是 {@link #UNAVAILABLE}。
 * 调用方不得把 {@link #UNAVAILABLE} 当成「没有发现问题」。</p>
 */
public enum LlmReviewStatus {

    /** 模型输出完整通过校验，结论可直接采纳。 */
    COMPLETED,

    /** 模型输出部分通过校验：非法条目已被丢弃，结论不完整。 */
    PARTIAL,

    /** 模型未接入、调用失败，或输出整体不可采信（超量、无可解析条目）。 */
    UNAVAILABLE
}
