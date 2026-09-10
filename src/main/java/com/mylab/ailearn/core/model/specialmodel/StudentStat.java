package com.mylab.ailearn.core.model.specialmodel;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 教师看板中的单个学生统计：错题数、提交数、最近活跃时间、是否待关注与其薄弱点。
 */
public record StudentStat(
        Long ownerUserId,
        long errorCount,
        long submissionCount,
        LocalDateTime lastActiveAt,
        boolean attention,
        List<WeakPoint> topWeakPoints) {

    public StudentStat {
        topWeakPoints = topWeakPoints == null ? List.of() : List.copyOf(topWeakPoints);
    }
}
