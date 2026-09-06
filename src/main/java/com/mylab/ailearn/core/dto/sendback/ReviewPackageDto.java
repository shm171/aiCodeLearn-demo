package com.mylab.ailearn.core.dto.sendback;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

/**
 * 学生错题复习聚合结果。功能文档第 3 点：
 * 专属刷题清单、薄弱知识点报告、错题分布饼图、易错知识点排行、月度学习曲线。
 *
 * <p>图表字段全部使用具体 DTO，前端可直接渲染；实现 Service 时按这些结构填充数据。
 */
@Getter
@AllArgsConstructor
public class ReviewPackageDto {

    /** 专属刷题清单：按错题频率和时间排序。 */
    private final List<ErrorRecordDto> practiceList;

    /** 薄弱知识点报告，按扣分权重降序。 */
    private final List<WeakTopicDto> weakTopics;

    /** 错题分类分布饼图数据。 */
    private final List<ChartSliceDto> categoryDistribution;

    /** 易错知识点排行（TOP N）。 */
    private final List<RankingItemDto> topicRanking;

    /** 月度学习曲线。 */
    private final List<MonthlyPointDto> monthlyCurve;
}
