package com.mylab.ailearn.core.service;

import com.mylab.ailearn.core.model.commonmodel.ErrorRecord;
import com.mylab.ailearn.core.model.commonmodel.SourceFile;
import com.mylab.ailearn.core.model.specialmodel.ClassDashboard;

import java.util.List;

/**
 * 教师数据看板：聚合班级整体错题数据，给授课提供参考。
 *
 * <p>三个重载只是数据来源不同：传入数据时是纯函数（便于复用与单测），无参时由实现自行从
 * 持久化端口取全量数据。</p>
 *
 * <p><b>数据范围与权限（重要）</b>：无参的 {@link #buildClassDashboard()} 取的是
 * <b>全库所有学生的错题与提交</b>，没有按教师所带班级过滤，本接口也不做角色校验。
 * 也就是说任何能调用到它的入口都会看到全部学生数据；「只有教师能看」以及
 * 「教师只能看自己班级」都需要在 Controller / Security 层另行实现。</p>
 */
public interface TeacherDashboardService {

    /**
     * 基于一组错题生成教师班级看板（不含提交数，提交相关字段为 0）。
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
     * 基于全库错题与提交生成教师班级看板。
     *
     * <p>依赖的持久化端口未实现时抛出 {@code IllegalStateException}（服务端错误，不是 400）。</p>
     *
     * @return 班级看板
     */
    ClassDashboard buildClassDashboard();
}
