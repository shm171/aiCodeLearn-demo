package com.mylab.ailearn.core.service;

import com.mylab.ailearn.core.model.commonmodel.ErrorRecord;
import com.mylab.ailearn.core.model.commonmodel.GradingResult;
import com.mylab.ailearn.core.model.commonmodel.SourceFile;
import com.mylab.ailearn.core.model.commonmodel.SubmissionGradingResult;
import com.mylab.ailearn.core.model.specialmodel.StudentDashboard;
import com.mylab.ailearn.core.enums.ErrorCategory;
import com.mylab.ailearn.core.enums.ErrorSeverity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * 顶层编排实现：把 7 个功能点串成完整流程。
 *
 * <p>这 7 个功能点落在 5 个被注入的 Service 上，注释里的编号与之一一对应：
 * ① 文件上传（内部复用 ② 题目匹配）、③ 规则静态检查 + ④ LLM 深度批改（同属双层批改）、
 * ⑤ 错误记录、⑥ 错题归档、⑦ 学生学习报告。</p>
 */
@Service
@RequiredArgsConstructor
public class AiLearnOrchestratorImpl implements AiLearnOrchestrator {

    // ① 文件上传（内部复用 ② 题目匹配）
    private final FileUploadService fileUploadService;
    // ③+④ 双层批改：规则静态检查 + LLM 深度批改
    private final GradingService gradingService;
    // ⑤ 错误记录
    private final ErrorRecordService errorRecordService;
    // ⑥ 错题归档
    private final ErrorArchiveService errorArchiveService;
    // ⑦ 学生学习报告
    private final StudentReportService studentReportService;

    /**
     * 写侧主流程，整体在一个事务里：上传落库、批改、错误记录任意一步失败都会全部回滚。
     */
    @Override
    @Transactional
    public SubmissionGradingResult submitAndGrade(Long ownerUserId, String filename, String content) {
        // ①+②：上传并解析匹配章节，落库得到 SourceFile
        SourceFile file = fileUploadService.receive(ownerUserId, filename, content);
        // ③+④：双层批改
        GradingResult result = gradingService.grade(file);
        // ⑤：错误记录落库
        errorRecordService.record(ownerUserId, file, result);
        return new SubmissionGradingResult(file, result);
    }

    /** 读侧：查询某学生已归档的全部错题。数据范围由传入的 ownerUserId 决定，本层不校验归属。 */
    @Override
    @Transactional(readOnly = true)
    public List<ErrorRecord> listErrorArchive(Long ownerUserId) {
        return errorArchiveService.listByOwner(ownerUserId);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ErrorRecord> listErrorArchive(
            Long ownerUserId,
            ErrorCategory category,
            ErrorSeverity severity,
            Pageable pageable) {
        return errorArchiveService.listByOwner(ownerUserId, category, severity, pageable);
    }

    /** 读侧：查询某学生已归档的全部源码提交（按提交时间倒序）。数据范围由传入的 ownerUserId 决定。 */
    @Override
    @Transactional(readOnly = true)
    public List<SourceFile> listMySubmissions(Long ownerUserId) {
        return fileUploadService.listByOwner(ownerUserId);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<SourceFile> listMySubmissions(Long ownerUserId, Pageable pageable) {
        return fileUploadService.listByOwner(ownerUserId, pageable);
    }

    /** 写侧：把某学生的一条错题标记为「已掌握」；归属校验在归档服务内部完成。 */
    @Override
    @Transactional
    public ErrorRecord markErrorMastered(Long ownerUserId, Long errorId) {
        return errorArchiveService.markMastered(ownerUserId, errorId);
    }

    /** 读侧：生成某学生的个人看板。数据范围由传入的 ownerUserId 决定。 */
    @Override
    @Transactional(readOnly = true)
    public StudentDashboard studentDashboard(Long ownerUserId) {
        return studentReportService.buildStudentDashboard(ownerUserId);
    }
}
