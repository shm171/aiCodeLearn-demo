package com.mylab.ailearn.core.model.specialmodel;

import com.mylab.ailearn.core.model.commonmodel.ErrorRecord;

import java.util.List;

/**
 * 学生个人看板：一次查询返回学生自己需要的全部统计结果。
 *
 * @param distribution    错题分类分布，用于饼图
 * @param topWeakPoints   薄弱知识点排行（最多 10 条），用于列表展示
 * @param practiceList    刷题清单，为全部错题的排序结果（见 {@code WeakPointReport#practiceList}）
 * @param studentCurve    个人月度学习曲线
 * @param totalErrors     错题总条数
 * @param totalSubmissions 提交总次数；只传错题构造时为 0
 * @param accuracyRate    正确率，取值 <b>0.0~1.0 的小数</b>（不是百分数，前端展示需要自行 ×100）；
 *                        定义为「没有产生任何错题的提交数 / 总提交数」，无提交时为 0
 * @param summary         一句话诊断文案
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
