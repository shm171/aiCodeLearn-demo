package com.mylab.ailearn.core.model.specialmodel;

import com.mylab.ailearn.core.model.commonmodel.ErrorRecord;

import java.util.List;

/**
 * 学生薄弱知识点报告：薄弱点排行 + 据此生成的专属刷题清单（按频率与时间排序的错题列表）。
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
