package com.mylab.ailearn.core.dto.sendback;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 错题单条摘要。
 *
 *
 */
@Getter
@AllArgsConstructor
public class ErrorRecordDto {

    private final Long errorId;

    private final Long submissionId;

    /** 对应 GradingResponse.issues 中的分类。 */
    private final String category;

    /** 严重程度（INFO/WARNING/ERROR 的字符串）。 */
    private final String severity;

    /** 错误代码片段，实现时需要进行做长度裁剪。 */
    private final String codeSnippet;

    /** 保存的修改建议。 */
    private final String suggestion;

    /** 归档时间（ISO 字符串或 epoch）。 */
    private final String createdAt;
}
