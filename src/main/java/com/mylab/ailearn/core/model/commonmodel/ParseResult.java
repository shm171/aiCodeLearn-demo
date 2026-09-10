package com.mylab.ailearn.core.model.commonmodel;

import com.mylab.ailearn.core.enums.CourseChapter;
import com.mylab.ailearn.core.enums.ProgrammingLanguage;

/**
 * 文件解析结果：从文件名与内容中提取出的提交元信息。
 *
 * <p>由 {@code ChapterMatchService#parse} 生成，是 {@link SourceFile} 中
 * 文件名、语言、章节三个字段的来源。</p>
 *
 * @param filename 规范化后的文件名（去首尾空白，只含字母、数字、下划线、连字符与点）
 * @param language 由文件名后缀识别出的语言（.cpp → C++，.java → Java）
 * @param chapter  按源码关键词打分匹配到的课程章节；无命中时为「综合练习」
 */
public record ParseResult(
        String filename,
        ProgrammingLanguage language,
        CourseChapter chapter) {
}
