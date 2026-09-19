package com.mylab.ailearn.core.controller;

import com.mylab.ailearn.base.global.configs.CurrentUserResolver;
import com.mylab.ailearn.base.global.configs.OpenApiConfig;
import com.mylab.ailearn.core.model.commonmodel.SourceFile;
import com.mylab.ailearn.core.model.commonmodel.SubmissionGradingResult;
import com.mylab.ailearn.core.service.AiLearnOrchestrator;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.Set;

/**
 * 作业提交与批改入口：一次上传即返回解析 + 批改结果（门面已把流程合并为 submitAndGrade 一次调用）。
 */
@RestController
@RequestMapping("/core/submissions")
@RequiredArgsConstructor
@Tag(name = "AI 核心 - 作业提交与批改",
        description = "上传 .cpp/.java 源码，一次完成文件归档、语言识别、章节匹配、双层批改（规则静态检查 + LLM 深度批改）与错题归档")
public class SubmissionController {

    /** 与 ProgrammingLanguage 枚举支持的后缀保持一致。 */
    private static final Set<String> ALLOWED_EXTENSIONS = Set.of(".java", ".cpp", ".cc", ".cxx");

    private final CurrentUserResolver currentUserResolver;

    private final AiLearnOrchestrator orchestrator;

    @PostMapping(consumes = "multipart/form-data")
    @Operation(summary = "上传源码并执行完整批改流程",
            description = "需要 JWT。上传 .cpp 或 .java 源码文件，返回归档后的源码信息（submissionId、语言、章节）"
                    + "与批改结果（总分、问题清单、反馈、各阶段状态）。",
            security = @SecurityRequirement(name = OpenApiConfig.BEARER_AUTH))
    public ResponseEntity<SubmissionGradingResult> uploadAndGrade(
            @RequestParam("file") MultipartFile file
    ) {
        Long ownerUserId = currentUserResolver.currentUserId();

        if (file == null || file.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "file is empty");
        }
        String filename = file.getOriginalFilename();
        if (filename == null || filename.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "file name is missing");
        }
        String lower = filename.toLowerCase(Locale.ROOT);
        if (ALLOWED_EXTENSIONS.stream().noneMatch(lower::endsWith)) {
            throw new ResponseStatusException(HttpStatus.UNSUPPORTED_MEDIA_TYPE,
                    "only .java / .cpp source files are supported");
        }

        String content;
        try {
            content = new String(file.getBytes(), StandardCharsets.UTF_8);
        } catch (IOException exception) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "failed to read file content");
        }
        SubmissionGradingResult result = orchestrator.submitAndGrade(ownerUserId, filename, content);
        return ResponseEntity.ok(result);
    }

    @GetMapping
    @Operation(summary = "分页查询我的提交历史",
            description = "需要 JWT。按提交时间倒序返回当前登录学生的源码提交列表，"
                    + "默认每页 10 条。只返回当前登录学生自己的提交。",
            security = @SecurityRequirement(name = OpenApiConfig.BEARER_AUTH))
    public ResponseEntity<Page<SourceFile>> listMySubmissions(
            @PageableDefault(size = 10) Pageable pageable
    ) {
        Long ownerUserId = currentUserResolver.currentUserId();

        return ResponseEntity.ok(orchestrator.listMySubmissions(ownerUserId, pageable));
    }

    @GetMapping("/{submissionId}")
    @Operation(summary = "查询单条提交详情",
            description = "需要 JWT。返回源码原文。提交不存在或不属于当前学生时返回 404。",
            security = @SecurityRequirement(name = OpenApiConfig.BEARER_AUTH))
    public ResponseEntity<SourceFile> getMySubmission(@PathVariable long submissionId) {
        Long ownerUserId = currentUserResolver.currentUserId();
        SourceFile file = orchestrator.listMySubmissions(ownerUserId).stream()
                .filter(f -> f.id() != null && f.id() == submissionId)
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "submission not found"));
        return ResponseEntity.ok(file);
    }
}
