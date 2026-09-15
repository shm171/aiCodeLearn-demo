package com.mylab.ailearn.core.model.specialmodel;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 教师看板中的单个学生统计。
 *
 * @param ownerUserId     学生用户 ID
 * @param errorCount      该学生的错题条数
 * @param submissionCount 该学生的提交次数；调用方未传提交列表时为 0
 * @param lastActiveAt    最近一次提交时间；无提交或无时间数据时为 null
 * @param attention       是否「待关注」：以该学生最新一条错题时间为基准，最近一个观察窗口的
 *                        错题数达到下限且相比上一窗口显著上升时为 true
 * @param topWeakPoints   该学生最突出的薄弱知识点（最多 3 条）
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
