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
 * 刷题清单与学生个人看板。
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
     * 基于一批错题生成薄弱知识点报告（含刷题清单与一句话诊断）。
     * 只传错题时，报告中的学习曲线与提交数相关字段均为 0。
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
     * 基于一批错题生成刷题清单：按「同类型错题的出现频率」降序、再按归档时间倒序排序。
     *
     * <p>返回的是<b>传入错题的排序副本</b>，不去重、也不排除已标记「已掌握」的记录；
     * 需要只展示未掌握的题目请在展示层过滤。</p>
     *
     * @param records 错题列表
     * @return 排序后的错题列表，条数与入参一致
     */
    List<ErrorRecord> buildPracticeList(List<ErrorRecord> records);

    /**
     * 把一批错题聚合成薄弱知识点排行，按权重降序返回前 {@code limit} 条。
     * 供学生报告与教师看板复用，保证口径统一。
     *
     * <p>权重 = 每条错题按其分类的扣分权重求和，并做时间衰减（半衰期 30 天）。
     * <b>衰减的时间基准是这批错题里最新的一条 {@code createdAt}，而不是系统当前时间</b>——
     * 这样同一批数据无论何时计算都得到相同结果，便于测试与横向对比。</p>
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
