package com.mylab.ailearn.core.service;

import com.mylab.ailearn.core.model.commonmodel.ErrorRecord;
import com.mylab.ailearn.core.model.commonmodel.SubmissionGradingResult;
import com.mylab.ailearn.core.model.specialmodel.ClassDashboard;
import com.mylab.ailearn.core.model.specialmodel.StudentDashboard;

import java.util.List;

/**
 * 顶层编排入口：把「文件上传、题目匹配、双层批改、错误记录、错题归档、
 * 学生报告、教师看板」这些零散功能串成完整的数据流转，是 Controller 层
 * 唯一需要依赖的业务门面。
 *
 * <p><b>写侧流转</b>：文件上传 → 题目匹配 → 双层批改（规则静态检查 + LLM 深度批改）→ 错误记录落库。</p>
 * <p><b>读侧流转</b>：错题归档 → 学生学习报告 / 教师班级看板。</p>
 *
 * <p>Controller 层只需注入本接口即可完成一条龙业务，无需关心内部各 Service 的调用顺序。</p>
 */
public interface AiLearnOrchestrator {

    /**
     * 完整写侧流程：接收一份源码，依次完成上传归档、语言识别与章节匹配、
     * 双层批改、错误记录落库，最后返回归档后的源码与批改结果。
     *
     * @param ownerUserId 提交者用户 ID
     * @param filename    源码文件名（.cpp / .java）
     * @param content     源码内容
     * @return 归档后的源码 + 批改结果（总分、问题清单、反馈、各阶段状态与分数可采纳性）
     * @throws org.springframework.web.server.ResponseStatusException 参数非法（400）时抛出
     */
    SubmissionGradingResult submitAndGrade(Long ownerUserId, String filename, String content);

    /**
     * 读侧：查询某学生已归档的全部错题，供复习展示。
     *
     * @param ownerUserId 学生用户 ID
     * @return 该学生的错题列表（无错题时返回空列表，不会为 null）
     */
    List<ErrorRecord> listErrorArchive(Long ownerUserId);

    /**
     * 写侧：把某学生的一条错题标记为「已掌握」，标记后该错题不再作为薄弱点重点推荐。
     *
     * @param ownerUserId 学生用户 ID
     * @param errorId     错题记录 ID
     * @return 更新后的错题记录
     * @throws org.springframework.web.server.ResponseStatusException 错题不存在或不属于该学生（404）时抛出
     */
    ErrorRecord markErrorMastered(Long ownerUserId, Long errorId);

    /**
     * 读侧：生成学生个人看板（错题分布、薄弱点排行、刷题清单、学习曲线、正确率等）。
     *
     * @param ownerUserId 学生用户 ID
     * @return 学生个人看板
     */
    StudentDashboard studentDashboard(Long ownerUserId);

    /**
     * 读侧：生成教师班级看板（全班错题分布、薄弱点排行、每个学生的统计、班级学习曲线等）。
     *
     * @return 班级看板
     */
    ClassDashboard classDashboard();
}
