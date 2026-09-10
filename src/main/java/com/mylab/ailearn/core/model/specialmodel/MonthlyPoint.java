package com.mylab.ailearn.core.model.specialmodel;

/**
 * 月度学习曲线中的一个点：该月新增错题数与提交总数。
 */
public record MonthlyPoint(
        int year,
        int month,
        long errorCount,
        long submissionCount) {
}
