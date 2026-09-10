package com.mylab.ailearn.core.model.specialmodel;

import java.util.List;

/**
 * 月度学习曲线，按月统计错题数量变化。
 */
public record LearningCurve(List<MonthlyPoint> points) {

    public LearningCurve {
        points = points == null ? List.of() : List.copyOf(points);
    }
}