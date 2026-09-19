package com.mylab.ailearn.core.model.commonmodel;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/** 根据错题生成类似练习的请求。 */
public record PracticeGenerationRequest(
        @NotBlank @Size(max = 255) String errorType,
        @Pattern(regexp = "FORMAT_ERROR|SYNTAX_ERROR|LOGIC_ERROR", message = "unknown category")
        String category,
        @Pattern(regexp = "CPP|JAVA", message = "language must be CPP or JAVA")
        String language) {
}
