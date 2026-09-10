package com.mylab.ailearn.core.model.commonmodel;

import com.mylab.ailearn.core.enums.CourseChapter;
import com.mylab.ailearn.core.enums.ProgrammingLanguage;

import java.time.LocalDateTime;

/**
 * 一份已解析的学生源码。id 为空表示尚未落库，由持久化组件回填。
 */
public record SourceFile(
        Long id,
        Long ownerUserId,
        String filename,
        ProgrammingLanguage language,
        CourseChapter chapter,
        String content,
        LocalDateTime submittedAt) {
}