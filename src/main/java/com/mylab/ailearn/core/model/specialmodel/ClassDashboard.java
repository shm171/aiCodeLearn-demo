package com.mylab.ailearn.core.model.specialmodel;

import java.util.List;

/**
 * 教师端班级看板：错题分布、易错知识点排行、学生统计、班级学习曲线、总错题数、
 * 总提交数与一句话诊断。
 */
public record ClassDashboard(
        ErrorDistribution distribution,
        List<WeakPoint> topWeakPoints,
        List<StudentStat> studentStats,
        LearningCurve classCurve,
        long totalErrors,
        long totalSubmissions,
        String summary) {

    public ClassDashboard {
        topWeakPoints = topWeakPoints == null ? List.of() : List.copyOf(topWeakPoints);
        studentStats = studentStats == null ? List.of() : List.copyOf(studentStats);
    }
}
