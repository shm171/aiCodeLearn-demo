package com.mylab.ailearn.core.controller;

import com.mylab.ailearn.base.global.configs.CurrentUserResolver;
import com.mylab.ailearn.base.global.configs.OpenApiConfig;
import com.mylab.ailearn.core.dto.sendback.TeacherDashboardDto;
import com.mylab.ailearn.core.service.TeacherDashboardService;
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

@RestController
@RequestMapping("/core/teacher")
@RequiredArgsConstructor
@Tag(name = "AI 核心 - 教师数据看板", description = "班级整体错题数据，给授课提供参考")
public class TeacherDashboardController {


    private final CurrentUserResolver currentUserResolver;


    private final TeacherDashboardService teacherDashboardService;

    @GetMapping("/dashboard")
    @Operation(summary = "获取教师看板数据",
            description = "需要 JWT。只有 TEACHER / ADMIN 角色应有权限；角色授权由 SecurityConfig 统一配置，" +
                    "Controller 不自己判断角色。返回班级总提交数、总错题数、易错知识点排行、" +
                    "每个学生错题统计、错题分类分布、待关注学生清单。" +
                    "若 classId 为空，返回该教师名下所有班级的聚合数据。",
            security = @SecurityRequirement(name = OpenApiConfig.BEARER_AUTH))
    public ResponseEntity<TeacherDashboardDto> getDashboard(
            @Parameter(description = "班级 ID；不传则返回该教师名下所有班级的聚合数据")
            @RequestParam(required = false) String classId
    ) {
        Long teacherUserId = currentUserResolver.currentUserId();
        return ResponseEntity.ok(teacherDashboardService.getDashboard(teacherUserId, classId));
    }
}
