package com.mylab.ailearn.core.dto.sendback;

import com.mylab.ailearn.core.dto.IssueSeverity;
import com.mylab.ailearn.core.dto.IssueSource;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 双层批改输出的单条问题记录。
 *
 *
 */
@Getter
@AllArgsConstructor
public class IssueDto {

    /** 该问题来源：规则校验 or LLM 深度批改。 */
    private final IssueSource source;

    /** 严重程度：INFO / WARNING / ERROR。 */
    private final IssueSeverity severity;

    /**
     * 规则或 LLM 标记的问题分类，例如 "命名规范"、"缩进"、"内存泄漏"、"数组越界"、"算法思路"。
     */
    private final String category;

    /** 定位到的代码行号，无法定位时为 null。 */
    private final Integer lineNumber;

    /** 问题描述。 */
    private final String message;

    /** 建议的修改方案，没有时为 null。 */
    private final String suggestion;
}
