package com.mylab.ailearn.core.controller;

import com.mylab.ailearn.base.global.configs.CurrentUserResolver;
import com.mylab.ailearn.base.global.configs.OpenApiConfig;
import com.mylab.ailearn.core.dto.sendback.SubmissionResponse;
import com.mylab.ailearn.core.service.SubmissionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/core/submissions")
@RequiredArgsConstructor
@Tag(name = "AI 核心 - 文件上传与解析", description = "接收 .cpp/.java 源码，提取文件名、识别语言、匹配课程章节")
public class SubmissionController {

    private final CurrentUserResolver currentUserResolver;

    private final SubmissionService submissionService;

    @PostMapping(consumes = "multipart/form-data")
    @Operation(summary = "上传源码并解析",
            description = "需要 JWT。上传 .cpp 或 .java 源码文件，返回解析结果和 submissionId。",
            security = @SecurityRequirement(name = OpenApiConfig.BEARER_AUTH))
    public ResponseEntity<SubmissionResponse> uploadSource(
            @RequestParam("file") MultipartFile file
    ) {

        Long ownerUserId = currentUserResolver.currentUserId();

        SubmissionResponse response = submissionService.upload(file, ownerUserId);
        return ResponseEntity.ok(response);
    }
}
