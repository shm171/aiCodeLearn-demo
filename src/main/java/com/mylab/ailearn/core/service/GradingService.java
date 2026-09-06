package com.mylab.ailearn.core.service;

import com.mylab.ailearn.core.dto.sendback.GradingResponse;
import com.mylab.ailearn.core.dto.sendto.GradingRequest;
import org.springframework.stereotype.Service;

/**
 * 规则校验 Tool + LLM 深度批改。
 *
 *
 * <ol>
 *   <li>读取 submission 的源码内容；</li>
 *   <li>按 Java/CPP 分发到不同的规则校验 Tool；</li>
 *   <li>如启用 LLM，调用 Spring AI Alibaba 与通义对接；</li>
 *   <li>聚合问题列表并排序；</li>
 *   <li>归档错题。</li>
 * </ol>
 *
 * <p>错题归档涉及的表结构/实体请先与负责人协调 Flyway 版本。
 */
@Service
public class GradingService {

    /**
     * 对给定 submission 执行双层批改。
     *
     * @param submissionId 来自文件上传接口返回的 submissionId
     * @param ownerUserId  发起批改的学生 ID，用于资源归属校验
     * @param request      是否启用 LLM 等批改选项
     */
    public GradingResponse grade(Long submissionId, Long ownerUserId, GradingRequest request) {

        return null;
    }
}
