package com.mylab.ailearn.core.dto.sendback;

import com.mylab.ailearn.core.dto.SubmissionLanguage;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 源码上传与解析结果。对应功能文档第 1 点"文件上传与解析"。
 *
 *
 */
@Getter
@AllArgsConstructor
public class SubmissionResponse {

    /** 解析后的提交记录 ID，后续批改、错题归档通过它关联本次上传。 */
    private final Long submissionId;

    /** 提取出的源码文件名。 */
    private final String fileName;

    /** 识别出的语言类型。 */
    private final SubmissionLanguage language;

    /**
     * 匹配到的课程章节。
     * TODO 章节匹配逻辑未实现。
     */
    private final String chapter;
}
