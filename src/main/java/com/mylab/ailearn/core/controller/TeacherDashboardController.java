package com.mylab.ailearn.core.controller;

import com.mylab.ailearn.base.global.configs.CurrentUserResolver;
import com.mylab.ailearn.base.global.configs.OpenApiConfig;
import com.mylab.ailearn.core.model.specialmodel.ClassDashboard;
import com.mylab.ailearn.core.service.AiLearnOrchestrator;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 教师数据看板入口。
 *
 * <p>当前版本门面只提供全班聚合视图（{@link AiLearnOrchestrator#classDashboard()}），
 * 按班级过滤的能力尚未实现，classId 参数暂时预留。</p>
 */
@RestController
@RequestMapping("/core/teacher")
@RequiredArgsConstructor
@Tag(name = "AI 核心 - 教师数据看板",
        description = "班级整体错题分布、薄弱知识点排行、每个学生的统计与待关注清单，给授课提供参考")
public class TeacherDashboardController {

    private final CurrentUserResolver currentUserResolver;

    private final AiLearnOrchestrator orchestrator;

    @GetMapping("/dashboard")
    @Operation(summary = "获取教师看板数据",
            description = "需要 JWT。只有 TEACHER / ADMIN 角色应有权限；角色授权由 SecurityConfig 统一配置。"
                    + "返回全班总提交数、总错题数、易错知识点排行、每个学生错题统计、错题分类分布、"
                    + "待关注学生清单与班级学习曲线。注意：当前版本返回所有班级的聚合数据。",
            security = @SecurityRequirement(name = OpenApiConfig.BEARER_AUTH))
    public ResponseEntity<ClassDashboard> getDashboard(
            @Parameter(description = "班级 ID（预留参数）：当前版本门面未支持按班级过滤，传入也会返回全局聚合数据")
            @RequestParam(required = false) String classId
    ) {
        // 取当前登录教师 ID 仅用于通过认证校验；看板数据由门面按全班维度聚合。
        currentUserResolver.currentUserId();
        return ResponseEntity.ok(orchestrator.classDashboard());
    }
}
