package com.mylab.ailearn.core.model.commonmodel;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** 根据错题生成类似练习的请求。 */
public record PracticeGenerationRequest(
        @NotBlank @Size(max = 255) String errorType,
        @Size(max = 30) String category,
        @Size(max = 20) String language) {
}
