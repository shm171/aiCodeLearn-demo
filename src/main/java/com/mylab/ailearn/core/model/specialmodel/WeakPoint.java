package com.mylab.ailearn.core.model.specialmodel;

import com.mylab.ailearn.core.enums.CourseChapter;
import com.mylab.ailearn.core.enums.ErrorCategory;

import java.time.LocalDateTime;

/**
 * 一个薄弱知识点：某一章节下某类错误出现的次数、累计扣分权重、掌握度与最近出现时间。
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
