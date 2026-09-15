package com.mylab.ailearn.core.model.specialmodel;

import com.mylab.ailearn.core.enums.CourseChapter;
import com.mylab.ailearn.core.enums.ErrorCategory;

import java.time.LocalDateTime;

/**
 * 一个薄弱知识点：同一「章节 + 错误分类 + 错误类型」下所有错题聚合出的统计。
 *
 * @param chapter    所属课程章节
 * @param category   错误大类
 * @param errorType  错误类型名，作为该知识点的展示名
 * @param count      该知识点累计出现的错题条数
 * @param weight     扣分权重合计：每条错题按其分类的权重累加，并按时间衰减
 *                   （越久远的错题贡献越小）。排序即按本值降序
 * @param mastery    掌握度，取值 0.0~1.0 = 该知识点下已标记「已掌握」的条数 / 总条数
 * @param lastSeenAt 该知识点最近一次出现的时间；所有记录都没有时间时为 null
 */
public record WeakPoint(
        CourseChapter chapter,
        ErrorCategory category,
        String errorType,
        long count,
        double weight,
        double mastery,
        LocalDateTime lastSeenAt) {
}
