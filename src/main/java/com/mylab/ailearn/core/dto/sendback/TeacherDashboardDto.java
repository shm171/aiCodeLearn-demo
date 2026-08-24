package com.mylab.ailearn.core.dto.sendback;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

/**
 * 教师端班级整体看板聚合结果。功能文档第 4 点：班级整体错题数据，给授课提供参考。
 *
 * <p>图表字段全部使用具体 DTO，前端可直接渲染。
 * 角色授权（只有 TEACHER/ADMIN 才能调）由 SecurityConfig 或 AOP 统一处理，不写在 Controller 内。
 */
@Getter
@AllArgsConstructor
public class TeacherDashboardDto {

    /** 班级内学生总提交数。 */
    private final Long totalSubmissions;

    /** 班级内学生累计错题数。 */
    private final Long totalErrors;

    /** 班级易错知识点排行（TOP N）。 */
    private final List<RankingItemDto> classTopicRanking;

    /** 每个学生的错题数量对比，按错题数降序，便于定位后进生。 */
    private final List<StudentErrorStatDto> perStudentStats;

    /** 班级错题分类分布饼图。 */
    private final List<ChartSliceDto> classCategoryDistribution;

    /** 待关注学生（最近错题频率显著上升）的 ownerUserId 清单。 */
    private final List<Long> attentionStudentIds;
}
