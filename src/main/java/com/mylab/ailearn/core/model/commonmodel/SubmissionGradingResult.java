package com.mylab.ailearn.core.model.commonmodel;

/**
 * 写侧完整流程的结果：已归档的源码 + 双层批改结果，一次上传调用即全部返回。
 */
public record SubmissionGradingResult(
        SourceFile sourceFile,
        GradingResult gradingResult) {
}
