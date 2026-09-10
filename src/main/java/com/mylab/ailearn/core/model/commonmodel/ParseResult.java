package com.mylab.ailearn.core.model.commonmodel;

import com.mylab.ailearn.core.enums.CourseChapter;
import com.mylab.ailearn.core.enums.ProgrammingLanguage;

/**
 * 文件解析结果：提取文件名、识别开发语言、匹配课程章节。
 */
public record ParseResult(
        String filename,
        ProgrammingLanguage language,
        CourseChapter chapter) {
}