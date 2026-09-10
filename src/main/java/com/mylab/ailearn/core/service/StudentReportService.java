package com.mylab.ailearn.core.service;

import com.mylab.ailearn.core.model.commonmodel.ErrorRecord;
import com.mylab.ailearn.core.model.commonmodel.SourceFile;
import com.mylab.ailearn.core.model.specialmodel.ErrorDistribution;
import com.mylab.ailearn.core.model.specialmodel.LearningCurve;
import com.mylab.ailearn.core.model.specialmodel.StudentDashboard;
import com.mylab.ailearn.core.model.specialmodel.WeakPoint;
import com.mylab.ailearn.core.model.specialmodel.WeakPointReport;

import java.util.List;

/**
 * 学生学习报告：基于错题与提交数据，生成薄弱知识点报告、错题分布、学习曲线、
 * 专属刷题清单与学生个人看板。
 *
 * <p>每个聚合方法都提供两种入参形式：</p>
 * <ul>
 *   <li>传 {@link List}{@code <ErrorRecord>}（纯函数，便于复用与单元测试）；</li>
 *   <li>传 {@code ownerUserId}（从数据库取数后走同样的纯函数逻辑）。</li>
 * </ul>
 * <p>部分看板 / 曲线方法还支持同时传入提交列表，以便统计每月提交数与正确率。</p>
 */
public interface StudentReportService {

    /**
     * 基于一批错题生成薄弱知识点报告（含专属刷题清单与一句话诊断）。
     *
     * @param records 错题列表
     * @return 薄弱知识点报告，永不返回 null
     */
    WeakPointReport buildWeakPointReport(List<ErrorRecord> records);

    /**
     * 基于某学生的错题生成薄弱知识点报告（含刷题清单）。
     *
     * @param ownerUserId 学生用户 ID
     * @return 薄弱知识点报告，永不返回 null
     */
    WeakPointReport buildWeakPointReport(Long ownerUserId);

    /**
     * 基于一批错题生成错题分布（饼图）数据。
     *
     * @param records 错题列表
     * @return 按错误分类统计的饼图数据
     */
    ErrorDistribution buildErrorDistribution(List<ErrorRecord> records);

    /**
     * 基于某学生的错题生成错题分布（饼图）数据。
     *
     * @param ownerUserId 学生用户 ID
     * @return 按错误分类统计的饼图数据
     */
    ErrorDistribution buildErrorDistribution(Long ownerUserId);

    /**
     * 基于一批错题生成月度学习曲线（折线）数据。
     *
     * @param records 错题列表
     * @return 按月统计的错题数量曲线
     */
    LearningCurve buildLearningCurve(List<ErrorRecord> records);

    /**
     * 基于一批错题与提交生成月度学习曲线（折线）数据，同时统计每月提交数。
     *
     * @param records     错题列表
     * @param submissions 源码提交列表
     * @return 按月统计的错题数与提交数曲线
     */
    LearningCurve buildLearningCurve(List<ErrorRecord> records, List<SourceFile> submissions);

    /**
     * 基于某学生的错题与提交生成学生个人月度学习曲线（折线）数据。
     *
     * @param ownerUserId 学生用户 ID
     * @return 按月统计的错题数与提交数曲线
     */
    LearningCurve buildLearningCurve(Long ownerUserId);

    /**
     * 基于一批错题生成专属刷题清单：按错题频率与归档时间倒序排序。
     *
     * @param records 错题列表
     * @return 排序后的刷题清单（即错题列表）
     */
    List<ErrorRecord> buildPracticeList(List<ErrorRecord> records);

    /**
     * 把一批错题聚合成薄弱知识点排行（含时间衰减扣分权重与最近出现时间），
     * 按权重降序返回前 {@code limit} 条。供学生报告与教师看板复用，保证口径统一。
     *
     * @param records 错题列表
     * @param limit   返回条数上限
     * @return 按权重降序的薄弱知识点列表
     */
    List<WeakPoint> aggregateWeakPoints(List<ErrorRecord> records, int limit);

    /**
     * 生成一句话诊断（指出最薄弱知识点）。供学生报告与教师看板复用。
     *
     * @param weakPoints 已排序的薄弱知识点列表
     * @return 一句话诊断文案
     */
    String summarizeWeakPoints(List<WeakPoint> weakPoints);

    /**
     * 基于一批错题生成学生个人看板（不含提交数，正确率按 0 处理）。
     *
     * @param records 错题列表
     * @return 学生个人看板
     */
    StudentDashboard buildStudentDashboard(List<ErrorRecord> records);

    /**
     * 基于一批错题与提交生成学生个人看板，同时统计每月提交数。
     *
     * @param records     错题列表
     * @param submissions 源码提交列表
     * @return 学生个人看板
     */
    StudentDashboard buildStudentDashboard(List<ErrorRecord> records, List<SourceFile> submissions);

    /**
     * 基于某学生的错题与提交生成学生个人看板。
     *
     * @param ownerUserId 学生用户 ID
     * @return 学生个人看板
     */
    StudentDashboard buildStudentDashboard(Long ownerUserId);
}
