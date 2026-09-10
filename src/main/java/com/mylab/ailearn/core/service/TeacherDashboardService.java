package com.mylab.ailearn.core.service;

import com.mylab.ailearn.core.model.commonmodel.ErrorRecord;
import com.mylab.ailearn.core.model.commonmodel.SourceFile;
import com.mylab.ailearn.core.model.specialmodel.ClassDashboard;

import java.util.List;

/**
 * 教师数据看板：批量查看班级整体错题数据，给授课提供参考。
 */
public interface TeacherDashboardService {

    /**
     * 基于一组错题生成教师班级看板（不含提交数）。
     *
     * @param records 全班错题列表
     * @return 班级看板
     */
    ClassDashboard buildClassDashboard(List<ErrorRecord> records);

    /**
     * 基于一组错题与提交生成教师班级看板，同时统计每个学生提交数与总提交数。
     *
     * @param records     全班错题列表
     * @param submissions 全班源码提交列表
     * @return 班级看板
     */
    ClassDashboard buildClassDashboard(List<ErrorRecord> records, List<SourceFile> submissions);

    /**
     * 基于全量错题与提交生成教师班级看板。
     *
     * @return 班级看板
     */
    ClassDashboard buildClassDashboard();
}
