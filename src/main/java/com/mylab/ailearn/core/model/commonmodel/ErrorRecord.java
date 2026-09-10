package com.mylab.ailearn.core.model.commonmodel;

import com.mylab.ailearn.core.enums.CourseChapter;
import com.mylab.ailearn.core.enums.ErrorCategory;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 归档的错题记录。记录错误类型、错误代码与修改方案，供复习与教师看板聚合使用。
 */
public record ErrorRecord(
        Long id,
        Long ownerUserId,
        Long sourceFileId,
        CourseChapter chapter,
        ErrorCategory category,
        String errorType,
        String errorCode,
        String fixSuggestion,
        List<Integer> line,
        LocalDateTime createdAt,
        boolean mastered) {

    public ErrorRecord {
        line = line == null ? List.of() : line.stream().distinct().sorted().toList();
    }
}
