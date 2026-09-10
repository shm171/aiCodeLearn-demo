package com.mylab.ailearn.core.model.commonmodel;

/**
 * 写侧完整流程的结果：已归档的源码 + 双层批改结果。
 *
 * <p>submissionId 即 {@code sourceFile.id()}；文件名、语言、章节取自 {@code sourceFile}，
 * 批改总分与问题清单取自 {@code gradingResult}。供 Mapper 分别转成
 * SubmissionResponse 与 GradingResponse。</p>
 */
public record SubmissionGradingResult(
        SourceFile sourceFile,
        GradingResult gradingResult) {
}
