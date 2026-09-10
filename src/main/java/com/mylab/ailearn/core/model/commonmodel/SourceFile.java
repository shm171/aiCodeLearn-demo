package com.mylab.ailearn.core.model.commonmodel;

import com.mylab.ailearn.core.enums.CourseChapter;
import com.mylab.ailearn.core.enums.ProgrammingLanguage;

import java.time.LocalDateTime;

/**
 * 一份已解析的学生源码。
 *
 * <p>写侧流程中，上传与批改都以本对象为载体；{@code content} 是本次批改的权威源码，
 * 行号、行数等判断一律以它为准。</p>
 *
 * @param id           提交 ID；null 表示尚未落库，由持久化组件回填
 * @param ownerUserId  提交者用户 ID
 * @param filename     文件名，已由 {@code SourceInputValidator} 校验并规范化（去首尾空白，
 *                     只含字母、数字、下划线、连字符与点）
 * @param language     识别出的编程语言
 * @param chapter      匹配到的课程章节
 * @param content      源码原文，不超过 256 KB 与 5000 行
 * @param submittedAt  提交时间（应用侧生成）
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
