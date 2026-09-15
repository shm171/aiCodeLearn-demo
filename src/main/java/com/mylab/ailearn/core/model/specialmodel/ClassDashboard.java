package com.mylab.ailearn.core.model.specialmodel;

import java.util.List;

/**
 * 教师端班级看板：全班维度的统计结果。
 *
 * @param distribution     全班错题分类分布，用于饼图
 * @param topWeakPoints    全班薄弱知识点排行（最多 10 条）
 * @param studentStats     每个学生的统计，按错题数从多到少排序
 * @param classCurve       班级月度学习曲线
 * @param totalErrors      全班错题总条数
 * @param totalSubmissions 全班提交总次数
 * @param summary          一句话诊断文案
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
