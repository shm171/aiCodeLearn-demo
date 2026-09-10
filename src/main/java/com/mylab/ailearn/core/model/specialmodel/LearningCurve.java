package com.mylab.ailearn.core.model.specialmodel;

import java.util.List;

/**
 * 月度学习曲线（折线图数据），按月升序排列。
 *
 * <p>每个点同时带该月的错题数与提交数：只传错题生成的曲线里 {@code submissionCount} 恒为 0。</p>
 *
 * @param points 按月聚合的数据点；无数据时为空列表，不为 null
 */
public record LearningCurve(List<MonthlyPoint> points) {

    public LearningCurve {
        points = points == null ? List.of() : List.copyOf(points);
    }
}
