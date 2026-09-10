package com.mylab.ailearn.core.service;

import com.mylab.ailearn.core.model.commonmodel.ErrorRecord;
import com.mylab.ailearn.core.model.commonmodel.GradingResult;
import com.mylab.ailearn.core.model.commonmodel.SourceFile;
import com.mylab.ailearn.core.model.commonmodel.SubmissionGradingResult;
import com.mylab.ailearn.core.model.specialmodel.ClassDashboard;
import com.mylab.ailearn.core.model.specialmodel.StudentDashboard;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 顶层编排实现：数据在 8 个功能类之间流转。
 */
@Service
@RequiredArgsConstructor
public class AiLearnOrchestratorImpl implements AiLearnOrchestrator {

    // 1 文件上传（内部复用 2 题目匹配）
    private final FileUploadService fileUploadService;
    // 双层批改：3 静态检查 + 4 LLM 深度批改
    private final GradingService gradingService;
    // 5 错误记录
    private final ErrorRecordService errorRecordService;
    // 6 错题归档
    private final ErrorArchiveService errorArchiveService;
    // 7 学生学习报告
    private final StudentReportService studentReportService;
    // 8 教师数据看板
    private final TeacherDashboardService teacherDashboardService;

    @Override
    @Transactional
    public SubmissionGradingResult submitAndGrade(Long ownerUserId, String filename, String content) {
        // 1+2：上传并解析匹配章节，落库得到 SourceFile
        SourceFile file = fileUploadService.receive(ownerUserId, filename, content);
        // 3+4：双层批改
        GradingResult result = gradingService.grade(file);
        // 5：错误记录落库
        errorRecordService.record(ownerUserId, file, result);
        return new SubmissionGradingResult(file, result);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ErrorRecord> listErrorArchive(Long ownerUserId) {
        return errorArchiveService.listByOwner(ownerUserId);
    }

    @Override
    @Transactional
    public ErrorRecord markErrorMastered(Long ownerUserId, Long errorId) {
        return errorArchiveService.markMastered(ownerUserId, errorId);
    }

    @Override
    @Transactional(readOnly = true)
    public StudentDashboard studentDashboard(Long ownerUserId) {
        return studentReportService.buildStudentDashboard(ownerUserId);
    }

    @Override
    @Transactional(readOnly = true)
    public ClassDashboard classDashboard() {
        return teacherDashboardService.buildClassDashboard();
    }
}
