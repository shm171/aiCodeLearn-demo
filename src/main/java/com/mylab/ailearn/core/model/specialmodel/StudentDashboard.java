package com.mylab.ailearn.core.model.specialmodel;

import com.mylab.ailearn.core.model.commonmodel.ErrorRecord;

import java.util.List;

/**
 * 学生个人看板：错题分布、易错知识点排行、专属刷题清单、个人学习曲线、
 * 总错题数、总提交数、正确率与一句话诊断。
 */
public record StudentDashboard(
        ErrorDistribution distribution,
        List<WeakPoint> topWeakPoints,
        List<ErrorRecord> practiceList,
        LearningCurve studentCurve,
        long totalErrors,
        long totalSubmissions,
        double accuracyRate,
        String summary) {

    public StudentDashboard {
        topWeakPoints = topWeakPoints == null ? List.of() : List.copyOf(topWeakPoints);
        practiceList = practiceList == null ? List.of() : List.copyOf(practiceList);
    }
}
