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
     * 深度批改。永不返回 null；调用失败时返回含降级说明的空 {@link LlmReview}，不抛异常。
     */
    LlmReview review(SourceFile file);
}