package com.mylab.ailearn.core.model.specialmodel;

import com.mylab.ailearn.core.model.commonmodel.ErrorRecord;

import java.util.List;

/**
 * 学生薄弱知识点报告：薄弱知识点排行 + 据此生成的刷题清单 + 一句话诊断。
 *
 * @param weakPoints   按权重降序的薄弱知识点排行（见 {@code StudentReportService#aggregateWeakPoints}）
 * @param practiceList 刷题清单：<b>全部错题记录</b>按「同类型错题出现频率」降序、再按归档时间倒序
 *                     的排序结果。它不去重、也不排除已标记「已掌握」的记录，
 *                     需要只读未掌握的题目请在展示层自行过滤
 * @param summary      一句话诊断文案
 */
public record WeakPointReport(
        List<WeakPoint> weakPoints,
        List<ErrorRecord> practiceList,
        String summary) {

    public WeakPointReport {
        weakPoints = weakPoints == null ? List.of() : List.copyOf(weakPoints);
        practiceList = practiceList == null ? List.of() : List.copyOf(practiceList);
    }
}
