package com.mylab.ailearn.core.model.specialmodel;

/**
 * 月度学习曲线中的一个点。
 *
 * @param year            年份
 * @param month           月份（1~12）
 * @param errorCount      该月新增的错题条数
 * @param submissionCount 该月的源码提交次数
 */
public record MonthlyPoint(
        int year,
        int month,
        long errorCount,
        long submissionCount) {
}
