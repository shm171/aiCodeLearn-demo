package com.mylab.ailearn.core.controller;

import com.mylab.ailearn.base.global.configs.CurrentUserResolver;
import com.mylab.ailearn.base.global.configs.OpenApiConfig;
import com.mylab.ailearn.core.dto.sendback.ErrorRecordDto;
import com.mylab.ailearn.core.dto.sendback.ReviewPackageDto;
import com.mylab.ailearn.core.dto.sendback.WeakTopicDto;
import com.mylab.ailearn.core.service.ReviewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/core/review")
@RequiredArgsConstructor
@Tag(name = "AI 核心 - 错题归档与复习",
        description = "学生错题列表、错题详情、专属刷题清单、薄弱知识点报告与学习图表")
public class ReviewController {

    private final CurrentUserResolver currentUserResolver;

    private final ReviewService reviewService;

    @GetMapping("/errors")
    @Operation(summary = "分页查询我的错题列表",
            description = "需要 JWT。支持按错误分类、严重程度过滤；默认每页 20 条，按归档时间倒序。" +
                    "资源归属校验（只返回当前学生的错题）由 Service 层完成。",
            security = @SecurityRequirement(name = OpenApiConfig.BEARER_AUTH))
    public ResponseEntity<Page<ErrorRecordDto>> listMyErrors(
            @Parameter(description = "错误分类过滤，例如 \"命名规范\"；不传则不过滤")
            @RequestParam(required = false) String category,
            @Parameter(description = "严重程度过滤：INFO / WARNING / ERROR；不传则不过滤")
            @RequestParam(required = false) String severity,
            @PageableDefault(size = 20) Pageable pageable
    ) {
        Long ownerUserId = currentUserResolver.currentUserId();
        return ResponseEntity.ok(reviewService.listMyErrors(ownerUserId, category, severity, pageable));
    }

    @GetMapping("/errors/{errorId}")
    @Operation(summary = "查询单条错题详情",
            description = "需要 JWT。若错题不属于当前学生，由 Service 返回 404。",
            security = @SecurityRequirement(name = OpenApiConfig.BEARER_AUTH))
    public ResponseEntity<ErrorRecordDto> getMyError(@PathVariable long errorId) {
        Long ownerUserId = currentUserResolver.currentUserId();
        return ResponseEntity.ok(reviewService.getMyError(ownerUserId, errorId));
    }

    @GetMapping("/package")
    @Operation(summary = "生成我的专属复习包",
            description = "需要 JWT。一次性返回刷题清单、薄弱知识点、错题分布饼图、易错知识点排行、月度学习曲线，" +
                    "供学生复习页直接渲染。",
            security = @SecurityRequirement(name = OpenApiConfig.BEARER_AUTH))
    public ResponseEntity<ReviewPackageDto> getMyReviewPackage() {
        Long ownerUserId = currentUserResolver.currentUserId();
        return ResponseEntity.ok(reviewService.buildMyReviewPackage(ownerUserId));
    }

    @GetMapping("/weak-topics")
    @Operation(summary = "查询我的薄弱知识点",
            description = "需要 JWT。按扣分权重降序返回，用于薄弱知识点报告独立加载。",
            security = @SecurityRequirement(name = OpenApiConfig.BEARER_AUTH))
    public ResponseEntity<List<WeakTopicDto>> getMyWeakTopics() {
        Long ownerUserId = currentUserResolver.currentUserId();
        return ResponseEntity.ok(reviewService.listMyWeakTopics(ownerUserId));
    }
}
