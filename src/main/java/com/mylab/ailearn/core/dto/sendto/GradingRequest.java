package com.mylab.ailearn.core.dto.sendto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

/**
 * 发起双层批改的请求参数。
 *
 *
 */
@Getter
@Setter
public class GradingRequest {

    /**
     * 是否启用 LLM 深度批改。
     * 默认关闭：只跑规则 Tool。
     */
    @Schema(description = "是否启用 LLM 深度批改，默认 false 只跑规则校验")
    private boolean enableLLM = false;
}
