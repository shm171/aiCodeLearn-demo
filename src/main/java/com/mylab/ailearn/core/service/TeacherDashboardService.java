package com.mylab.ailearn.core.service;

import com.mylab.ailearn.core.dto.sendback.TeacherDashboardDto;
import org.springframework.stereotype.Service;

/**
 * 教师数据看板的服务契约。
 *
 *
 * 角色授权（只有 TEACHER/ADMIN 可访问）应由 SecurityConfig 统一配置拦截规则，
 * 或通过 AOP/注解在 Service 层做越权检查，Controller 不重复写角色判断。
 */
@Service
public class TeacherDashboardService {

    /**
     * 按班级维度聚合看板数据。
     *
     * @param teacherUserId 当前登录教师 ID
     * @param classId       班级 ID，为 null 则返回该教师名下所有班级的聚合数据
     */
    public TeacherDashboardDto getDashboard(Long teacherUserId, String classId) {

        return null;
    }
}
