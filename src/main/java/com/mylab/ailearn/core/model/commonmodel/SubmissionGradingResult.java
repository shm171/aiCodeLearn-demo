package com.mylab.ailearn.core.model.commonmodel;

/**
 * 写侧完整流程的结果：已归档的源码 + 双层批改结果。
 *
 * <p>submissionId 即 {@code sourceFile.id()}；文件名、语言、章节取自 {@code sourceFile}，
 * 批改总分、问题清单与完成度状态取自 {@code gradingResult}。供 Controller 层拆成
 * 「提交信息」与「批改结果」两个响应对象返回给前端。</p>
 */
public record SubmissionGradingResult(
        SourceFile sourceFile,
        GradingResult gradingResult) {
}
