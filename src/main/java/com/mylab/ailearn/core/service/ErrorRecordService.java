package com.mylab.ailearn.core.service;

import com.mylab.ailearn.core.model.commonmodel.ErrorRecord;
import com.mylab.ailearn.core.model.commonmodel.GradingResult;
import com.mylab.ailearn.core.model.commonmodel.SourceFile;

import java.util.List;

/**
 * 错误记录：把一次批改产生的全部错误（错误类型、错误代码、行号、修改方案）落库。
 *
 * <p>这是写侧流程的最后一环，落库后的错题会被错题归档 / 学生报告 / 教师看板读取。</p>
 */
public interface ErrorRecordService {

    /**
     * 记录一次批改产生的全部错误。
     *
     * @param ownerUserId   提交者用户 ID
     * @param sourceFile    已归档的源码（用于取文件 ID 与章节）
     * @param gradingResult 批改结果（含问题清单）
     * @return 落库后的错题列表（本次批改无误时返回空列表，不会为 null）
     * @throws org.springframework.web.server.ResponseStatusException ownerUserId 或 sourceFile 无效时抛 400
     */
    List<ErrorRecord> record(Long ownerUserId, SourceFile sourceFile, GradingResult gradingResult);
}
