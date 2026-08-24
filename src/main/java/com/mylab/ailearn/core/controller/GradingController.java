package com.mylab.ailearn.core.controller;

import com.mylab.ailearn.base.global.configs.CurrentUserResolver;
import com.mylab.ailearn.base.global.configs.OpenApiConfig;
import com.mylab.ailearn.core.dto.sendback.GradingResponse;
import com.mylab.ailearn.core.dto.sendto.GradingRequest;
import com.mylab.ailearn.core.service.GradingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/core/submissions")
@RequiredArgsConstructor
@Tag(name = "AI 核心 - 双层批改",
        description = "规则校验 Tool（命名/缩进/越界等）+ LLM 深度批改（逻辑/算法思路）")
public class GradingController {

    private final CurrentUserResolver currentUserResolver;


    private final GradingService gradingService;

    @PostMapping("/{submissionId}/grade")
    @Operation(summary = "对指定提交执行双层批改",
            description = "需要 JWT。默认只跑规则校验；请求体 enableLLM=true 时追加通义深度批改。" +
                    "归属校验（submission 是否属于当前学生）由 Service 层完成。",
            security = @SecurityRequirement(name = OpenApiConfig.BEARER_AUTH))
    public ResponseEntity<GradingResponse> grade(
            @PathVariable long submissionId,
            @Valid @RequestBody GradingRequest request
    ) {
        Long ownerUserId = currentUserResolver.currentUserId();
        GradingResponse response = gradingService.grade(submissionId, ownerUserId, request);
        return ResponseEntity.ok(response);
    }
}
