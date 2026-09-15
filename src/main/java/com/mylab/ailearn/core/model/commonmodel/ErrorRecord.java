package com.mylab.ailearn.core.model.commonmodel;

import com.mylab.ailearn.core.enums.CourseChapter;
import com.mylab.ailearn.core.enums.ErrorCategory;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 归档的错题记录：一次批改中某一条问题落库后的形态。
 *
 * <p>由 {@code ErrorRecordService} 从 {@link GradedError} 转换而来，供错题归档、学生报告与
 * 教师看板聚合读取。</p>
 *
 * @param id           记录 ID；null 表示尚未落库，由持久化组件回填
 * @param ownerUserId  错题归属的学生用户 ID，由写侧入口传入
 * @param sourceFileId 产生该错题的源码提交 ID，对应 {@link SourceFile#id()}；用于统计"无错题的提交"
 * @param chapter      该错题所属课程章节，来自提交时的章节匹配结果
 * @param category     错误大类；可能为 null（规则或模型未给出分类时）
 * @param errorType    错误类型名，例如"数组越界访问"
 * @param errorCode    错误代码，例如 ARRAY_OUT_OF_BOUNDS
 * @param fixSuggestion 修改建议文案，可为 null
 * @param line         出错行号列表，行号从 1 开始；无法定位时为空列表
 * @param createdAt    归档时间（应用侧生成，不是数据库默认值）
 * @param mastered     是否已被学生标记为「已掌握」；true 表示不再作为薄弱点重点推荐
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
