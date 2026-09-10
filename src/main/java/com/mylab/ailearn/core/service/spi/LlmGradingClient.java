package com.mylab.ailearn.core.service.spi;


import com.mylab.ailearn.core.model.commonmodel.LlmReview;
import com.mylab.ailearn.core.model.commonmodel.SourceFile;

/**
 * LLM 深度批改端口（SPI）。
 *
 * <p>由负责 Spring AI Alibaba / 通义的同学实现，对规则筛不出的逻辑错误、
 * 算法思路问题进行深度批改。Service 层只依赖该抽象，不绑定具体模型。</p>
 */
public interface LlmGradingClient {

    /**
     * 深度批改。永不返回 null；调用失败时返回状态为
     * {@link com.mylab.ailearn.core.model.commonmodel.LlmReviewStatus#UNAVAILABLE} 的空
     * {@link LlmReview}，不抛异常。
     *
     * <p>实现返回的是<b>未经信任的模型原始输出</b>：调用方（{@code GradingServiceImpl}）
     * 会先经 {@code LlmReviewValidator} 校验分类、长度与行号范围，再决定是否进入分数与归档。
     * 实现方不得自行把结果标记为可信。</p>
     */
    LlmReview review(SourceFile file);
}