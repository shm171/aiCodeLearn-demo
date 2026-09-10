package com.mylab.ailearn.core.service;

import com.mylab.ailearn.core.model.commonmodel.ErrorRecord;
import com.mylab.ailearn.core.model.commonmodel.SourceFile;
import com.mylab.ailearn.core.model.specialmodel.ClassDashboard;
import com.mylab.ailearn.core.model.specialmodel.ErrorDistribution;
import com.mylab.ailearn.core.model.specialmodel.LearningCurve;
import com.mylab.ailearn.core.model.specialmodel.StudentStat;
import com.mylab.ailearn.core.model.specialmodel.WeakPoint;
import com.mylab.ailearn.core.service.spi.ErrorRecordStore;
import com.mylab.ailearn.core.service.spi.SourceFileStore;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 教师数据看板的默认实现。
 *
 * <p>班级聚合复用 {@link StudentReportService} 的错题分布与学习曲线等通用聚合原语
 * （权重与排序口径与学生报告完全一致），本身只编排班级维度结果。</p>
 *
 * <p>无参的 {@link #buildClassDashboard()} 直接取全量数据，不做教师 / 班级过滤，
 * 详见接口注释中的数据范围说明。</p>
 */
@Service
@RequiredArgsConstructor
public class TeacherDashboardServiceImpl implements TeacherDashboardService {

    private static final int TOP_WEAK_POINTS = 10;
    private static final int TOP_STUDENT_WEAK_POINTS = 3;

    /** 待关注观察窗口（天）：以最新错题时间为基准，向前观察一个窗口。 */
    private static final int ATTENTION_WINDOW_DAYS = 7;
    /** 待关注：近期错题绝对下限，低于该值不标记。 */
    private static final long ATTENTION_MIN_RECENT_ERRORS = 3L;
    /** 待关注：相比上一窗口的最小绝对增量。 */
    private static final long ATTENTION_MIN_RISE = 2L;
    /** 待关注：相比上一窗口的上升倍数阈值。 */
    private static final double ATTENTION_RISE_RATIO = 1.5;

    private final ObjectProvider<ErrorRecordStore> errorRecordStoreProvider;
    private final ObjectProvider<SourceFileStore> sourceFileStoreProvider;
    private final StudentReportService studentReportService;

    /** 基于一组错题生成教师班级看板。 */
    @Override
    public ClassDashboard buildClassDashboard(List<ErrorRecord> records) {
        return buildClassDashboard(records, List.of());
    }

    /** 基于一组错题与提交生成教师班级看板，同时统计每个学生提交数与总提交数。 */
    @Override
    public ClassDashboard buildClassDashboard(List<ErrorRecord> records, List<SourceFile> submissions) {
        // 空列表兜底，避免调用方传 null 导致后续 NPE
        List<ErrorRecord> data = ServiceSupport.nullToEmpty(records);
        List<SourceFile> files = ServiceSupport.nullToEmpty(submissions);
        // 根据错误记录，创建全班学生的错题扇形分布图
        ErrorDistribution distribution = studentReportService.buildErrorDistribution(data);
        // 生成全班学生的错题前十薄弱点
        List<WeakPoint> topWeakPoints = studentReportService.aggregateWeakPoints(data, TOP_WEAK_POINTS);
        // 教师看板中的单个学生看板，生成看板列表
        List<StudentStat> studentStats = buildStudentStats(data, files);
        // 生成班级月度学习曲线
        LearningCurve classCurve = studentReportService.buildLearningCurve(data, files);

        // 汇总成班级看板
        return new ClassDashboard(distribution, topWeakPoints, studentStats, classCurve,
                data.size(), files.size(), studentReportService.summarizeWeakPoints(topWeakPoints));
    }


    /** 基于全量错题生成教师班级看板。 */
    @Override
    @Transactional(readOnly = true)
    public ClassDashboard buildClassDashboard() {
        return buildClassDashboard(store().findAll(), sourceFileStore().findAll());
    }

    private List<StudentStat> buildStudentStats(List<ErrorRecord> records, List<SourceFile> submissions) {
        // 按 ownerUserId 分组；没有归属用户的记录视为脏数据直接跳过
        Map<Long, List<ErrorRecord>> byOwner = records.stream()
                .filter(record -> record.ownerUserId() != null)
                .collect(Collectors.groupingBy(ErrorRecord::ownerUserId, LinkedHashMap::new, Collectors.toList()));
        // 计算每一个用户的提交次数，刷新每一个用户最新提交时间，map
        Map<Long, Long> submissionCounts = new LinkedHashMap<>();
        Map<Long, LocalDateTime> lastActive = new LinkedHashMap<>();
        for (SourceFile file : submissions) {
            if (file.ownerUserId() == null) {
                continue;
            }
            submissionCounts.merge(file.ownerUserId(), 1L, Long::sum);
            if (file.submittedAt() != null) {
                LocalDateTime current = lastActive.get(file.ownerUserId());
                if (current == null || file.submittedAt().isAfter(current)) {
                    lastActive.put(file.ownerUserId(), file.submittedAt());
                }
            }
        }

        // 生成教师看板的单个学生看板，聚合成列表
        List<StudentStat> stats = new ArrayList<>();
        for (Map.Entry<Long, List<ErrorRecord>> entry : byOwner.entrySet()) {
            List<WeakPoint> top = studentReportService.aggregateWeakPoints(entry.getValue(), TOP_STUDENT_WEAK_POINTS);
            long errorCount = entry.getValue().size();
            long submissionCount = submissionCounts.getOrDefault(entry.getKey(), 0L);
            stats.add(new StudentStat(entry.getKey(), errorCount, submissionCount,
                    lastActive.get(entry.getKey()), isAttention(entry.getValue()), top));
        }
        // 按错题数量从大到小排序
        stats.sort(Comparator.comparingLong(StudentStat::errorCount).reversed());
        return stats;
    }

    /**
     * 待关注判定：以该学生最新一条错题时间为基准，比较「最近一个窗口」与「上一窗口」的错题数。
     * 仅当近期错题达到绝对下限、且相比上一窗口出现显著上升（同时满足绝对增量与倍数）时标记。
     */
    private boolean isAttention(List<ErrorRecord> studentErrors) {
        LocalDateTime now = studentErrors.stream()
                .map(ErrorRecord::createdAt)
                .filter(Objects::nonNull)
                .max(LocalDateTime::compareTo)
                .orElse(null);
        if (now == null) {
            return false;
        }
        LocalDateTime recentStart = now.minusDays(ATTENTION_WINDOW_DAYS);
        LocalDateTime previousStart = now.minusDays(2L * ATTENTION_WINDOW_DAYS);

        long recent = studentErrors.stream()
                .filter(r -> r.createdAt() != null)
                .filter(r -> !r.createdAt().isBefore(recentStart))
                .filter(r -> !r.createdAt().isAfter(now))
                .count();

        long previous = studentErrors.stream()
                .filter(r -> r.createdAt() != null)
                .filter(r -> !r.createdAt().isBefore(previousStart))
                .filter(r -> r.createdAt().isBefore(recentStart))
                .count();

        return recent >= ATTENTION_MIN_RECENT_ERRORS
                && recent >= previous + ATTENTION_MIN_RISE
                && recent >= previous * ATTENTION_RISE_RATIO;
    }

    /** 解析错题持久化端口（SPI）。 */
    private ErrorRecordStore store() {
        return ServiceSupport.required(errorRecordStoreProvider, "ErrorRecordStore");
    }

    /** 解析源码文件持久化端口（SPI）。 */
    private SourceFileStore sourceFileStore() {
        return ServiceSupport.required(sourceFileStoreProvider, "SourceFileStore");
    }
}
