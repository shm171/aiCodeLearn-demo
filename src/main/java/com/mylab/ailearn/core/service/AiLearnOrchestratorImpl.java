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
 * 顶层编排实现：把 8 个功能点串成完整流程。
 *
 * <p>这 8 个功能点落在 6 个被注入的 Service 上，注释里的编号与之一一对应：
 * ① 文件上传（内部复用 ② 题目匹配）、③ 规则静态检查 + ④ LLM 深度批改（同属双层批改）、
 * ⑤ 错误记录、⑥ 错题归档、⑦ 学生学习报告、⑧ 教师数据看板。</p>
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
    // ⑧ 教师数据看板
    private final TeacherDashboardService teacherDashboardService;

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

    /**
     * 读侧：生成教师班级看板。
     *
     * <p>取的是<b>全库</b>错题与提交（{@code findAll()}），没有按教师所带班级过滤，
     * 本层也不做角色校验——角色门槛需要由 Controller / Security 层补上。</p>
     */
    @Override
    @Transactional(readOnly = true)
    public ClassDashboard classDashboard() {
        return teacherDashboardService.buildClassDashboard();
    }
}
