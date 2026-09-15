package com.mylab.ailearn.core.model.commonmodel;

import com.mylab.ailearn.core.enums.CourseChapter;
import com.mylab.ailearn.core.enums.ProgrammingLanguage;

/**
 * 文件解析结果：文件名、语言、章节元信息；章节无关键词命中时为「综合练习」。
 */
public record ParseResult(
        String filename,
        ProgrammingLanguage language,
        CourseChapter chapter) {
}
