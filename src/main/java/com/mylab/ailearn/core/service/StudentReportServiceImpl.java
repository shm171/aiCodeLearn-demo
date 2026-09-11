package com.mylab.ailearn.core.service;

import com.mylab.ailearn.core.enums.ErrorCategory;
import com.mylab.ailearn.core.model.commonmodel.ErrorRecord;
import com.mylab.ailearn.core.model.commonmodel.SourceFile;
import com.mylab.ailearn.core.model.specialmodel.ErrorDistribution;
import com.mylab.ailearn.core.model.specialmodel.LearningCurve;
import com.mylab.ailearn.core.model.specialmodel.MonthlyPoint;
import com.mylab.ailearn.core.model.specialmodel.PieSlice;
import com.mylab.ailearn.core.model.specialmodel.StudentDashboard;
import com.mylab.ailearn.core.model.specialmodel.WeakPoint;
import com.mylab.ailearn.core.model.specialmodel.WeakPointReport;
import com.mylab.ailearn.core.service.spi.ErrorRecordStore;
import com.mylab.ailearn.core.service.spi.SourceFileStore;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;

/**
 * 学生学习报告的默认实现。
 *
 * <p>所有聚合逻辑都是纯函数（只依赖传入的数据），便于单元测试与在教师看板中复用；
 * 带 {@code ownerUserId} 的重载通过 ErrorRecordStore / SourceFileStore 端口取数后，
 * 走的是同一套纯函数逻辑。</p>
 */
@Service
@RequiredArgsConstructor
public class StudentReportServiceImpl implements StudentReportService {

    /** 薄弱知识点排行默认取前 10 条。 */
    private static final int TOP_WEAK_POINTS = 10;

    /** 权重时间衰减半衰期（天）：每过半衰期，历史错题对权重的贡献减半。 */
    private static final int WEIGHT_HALF_LIFE_DAYS = 30;

    private final ObjectProvider<ErrorRecordStore> errorRecordStoreProvider;
    private final ObjectProvider<SourceFileStore> sourceFileStoreProvider;

    /** 基于一批错题生成薄弱知识点报告（含刷题清单）。 */
    @Override
    public WeakPointReport buildWeakPointReport(List<ErrorRecord> records) {
        List<ErrorRecord> data = ServiceSupport.nullToEmpty(records);
        List<WeakPoint> weakPoints = aggregateWeakPoints(data, TOP_WEAK_POINTS);
        List<ErrorRecord> practiceList = buildPracticeList(data);
        String summary = summarizeWeakPoints(weakPoints);
        return new WeakPointReport(weakPoints, practiceList, summary);
    }

    /** 基于某学生的错题生成薄弱知识点报告（含刷题清单）。 */
    @Override
    @Transactional(readOnly = true)
    public WeakPointReport buildWeakPointReport(Long ownerUserId) {
        return buildWeakPointReport(store().findByOwnerUserId(ownerUserId));
    }

    /** 基于一批错题生成错题分布（饼图）数据。 */
    @Override
    public ErrorDistribution buildErrorDistribution(List<ErrorRecord> records) {
        List<ErrorRecord> data = ServiceSupport.nullToEmpty(records);
        Map<ErrorCategory, Long> counts = new EnumMap<>(ErrorCategory.class);
        for (ErrorRecord record : data) {
            if (record.category() != null) {
                counts.merge(record.category(), 1L, Long::sum);
            }
        }

        List<PieSlice> slices = new ArrayList<>();
        for (ErrorCategory category : ErrorCategory.values()) {
            long value = counts.getOrDefault(category, 0L);
            if (value > 0) {
                slices.add(new PieSlice(category.label(), value));
            }
        }
        return new ErrorDistribution(slices, data.size());
    }

    /** 基于一批错题生成月度学习曲线（折线）数据。 */
    @Override
    public LearningCurve buildLearningCurve(List<ErrorRecord> records) {
        return buildLearningCurve(records, List.of());
    }

    /** 基于一批错题与提交生成月度学习曲线（折线）数据，同时统计每月提交数。 */
    @Override
    public LearningCurve buildLearningCurve(List<ErrorRecord> records, List<SourceFile> submissions) {
        Map<YearMonth, long[]> byMonth = new TreeMap<>();
        for (ErrorRecord record : ServiceSupport.nullToEmpty(records)) {
            if (record.createdAt() != null) {
                byMonth.computeIfAbsent(YearMonth.from(record.createdAt()), k -> new long[2])[0]++;
            }
        }
        for (SourceFile submission : ServiceSupport.nullToEmpty(submissions)) {
            if (submission.submittedAt() != null) {
                byMonth.computeIfAbsent(YearMonth.from(submission.submittedAt()), k -> new long[2])[1]++;
            }
        }

        List<MonthlyPoint> points = byMonth.entrySet().stream()
                .map(entry -> new MonthlyPoint(
                        entry.getKey().getYear(),
                        entry.getKey().getMonthValue(),
                        entry.getValue()[0],
                        entry.getValue()[1]))
                .collect(Collectors.toList());
        return new LearningCurve(points);
    }

    /** 基于一批错题生成刷题清单：按错题频率与归档时间倒序排序（不去重）。 */
    @Override
    public List<ErrorRecord> buildPracticeList(List<ErrorRecord> records) {
        List<ErrorRecord> data = ServiceSupport.nullToEmpty(records);
        Map<String, Long> frequency = new LinkedHashMap<>();
        for (ErrorRecord record : data) {
            frequency.merge(keyOf(record), 1L, Long::sum);
        }
        return data.stream()
                .sorted(Comparator
                        .comparingLong((ErrorRecord r) -> frequency.getOrDefault(keyOf(r), 0L)).reversed()
                        .thenComparing(Comparator.comparing(
                                (ErrorRecord r) -> r.createdAt() == null ? LocalDateTime.MIN : r.createdAt(),
                                Comparator.reverseOrder())))
                .collect(Collectors.toList());
    }

    /** 基于一批错题生成学生个人看板。 */
    @Override
    public StudentDashboard buildStudentDashboard(List<ErrorRecord> records) {
        return buildStudentDashboard(records, List.of());
    }

    /** 基于一批错题与提交生成学生个人看板，同时统计每月提交数。 */
    @Override
    public StudentDashboard buildStudentDashboard(List<ErrorRecord> records, List<SourceFile> submissions) {
        List<ErrorRecord> data = ServiceSupport.nullToEmpty(records);
        List<SourceFile> files = ServiceSupport.nullToEmpty(submissions);

        ErrorDistribution distribution = buildErrorDistribution(data);
        List<WeakPoint> topWeakPoints = aggregateWeakPoints(data, TOP_WEAK_POINTS);
        List<ErrorRecord> practiceList = buildPracticeList(data);
        LearningCurve studentCurve = buildLearningCurve(data, files);

        long totalSubmissions = files.size();
        double accuracyRate = accuracyRate(data, files);

        return new StudentDashboard(distribution, topWeakPoints, practiceList, studentCurve,
                data.size(), totalSubmissions, accuracyRate, summarizeWeakPoints(topWeakPoints));
    }

    /** 基于某学生的错题生成学生个人看板。 */
    @Override
    @Transactional(readOnly = true)
    public StudentDashboard buildStudentDashboard(Long ownerUserId) {
        return buildStudentDashboard(
                store().findByOwnerUserId(ownerUserId),
                sourceFileStore().findByOwnerUserId(ownerUserId));
    }

    /**
     * 把一批错题聚合成薄弱知识点排行，按权重降序返回前 {@code limit} 条。
     *
     * <p>衰减基准取这批错题里最新的 {@code createdAt}（不是 {@code LocalDateTime.now()}），
     * 保证同一批数据的结果可复现。</p>
     */
    @Override
    public List<WeakPoint> aggregateWeakPoints(List<ErrorRecord> records, int limit) {
        if (limit <= 0) {
            return List.of();
        }
        List<ErrorRecord> data = ServiceSupport.nullToEmpty(records);
        LocalDateTime now = data.stream()
                .map(ErrorRecord::createdAt)
                .filter(Objects::nonNull)
                .max(LocalDateTime::compareTo)
                .orElse(null);

        Map<String, List<ErrorRecord>> groups = new LinkedHashMap<>();
        for (ErrorRecord record : data) {
            groups.computeIfAbsent(keyOf(record), k -> new ArrayList<>()).add(record);
        }

        List<WeakPoint> result = new ArrayList<>();
        for (List<ErrorRecord> group : groups.values()) {
            ErrorRecord first = group.get(0);
            long count = group.size();
            double weight = group.stream().mapToDouble(r -> weightedDeduction(r, now)).sum();
            double mastery = count == 0 ? 0.0
                    : (double) group.stream().filter(ErrorRecord::mastered).count() / count;
            LocalDateTime lastSeen = group.stream()
                    .map(ErrorRecord::createdAt)
                    .filter(Objects::nonNull)
                    .max(LocalDateTime::compareTo)
                    .orElse(null);
            result.add(new WeakPoint(first.chapter(), first.category(), first.errorType(),
                    count, weight, mastery, lastSeen));
        }
        result.sort(Comparator.comparingDouble(WeakPoint::weight).reversed());
        return result.size() > limit ? new ArrayList<>(result.subList(0, limit)) : result;
    }

    private String keyOf(ErrorRecord record) {
        return (record.chapter() == null ? "" : record.chapter().name())
                + "|" + (record.category() == null ? "" : record.category().name())
                + "|" + (record.errorType() == null ? "" : record.errorType());
    }

    private double weightedDeduction(ErrorRecord record, LocalDateTime now) {
        double weight = deductionWeight(record.category());
        if (record.createdAt() != null && now != null) {
            weight *= decay(record.createdAt(), now);
        }
        return weight;
    }

    private int deductionWeight(ErrorCategory category) {
        return category == null ? 0 : category.deductionWeight();
    }

    private double decay(LocalDateTime createdAt, LocalDateTime now) {
        long ageDays = ChronoUnit.DAYS.between(createdAt, now);
        return Math.pow(0.5, ageDays / (double) WEIGHT_HALF_LIFE_DAYS);
    }

    /**
     * 正确率 = 已落库提交中「没有产生任何错题」的占比（0.0 ~ 1.0）。
     *
     * <p>只统计有 id 的提交：没有 id 的提交既无法与错题关联、也无从判断对错，
     * 把它们算进分母会凭空拉低正确率（分子本来就按有 id 的提交统计）。
     * 没有任何可判断的提交时返回 0。</p>
     */
    private double accuracyRate(List<ErrorRecord> records, List<SourceFile> submissions) {
        List<Long> submissionIds = submissions.stream()
                .map(SourceFile::id)
                .filter(Objects::nonNull)
                .toList();
        if (submissionIds.isEmpty()) {
            return 0.0;
        }
        Set<Long> errorSourceFileIds = records.stream()
                .map(ErrorRecord::sourceFileId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        long cleanSubmissions = submissionIds.stream()
                .filter(id -> !errorSourceFileIds.contains(id))
                .count();
        return cleanSubmissions / (double) submissionIds.size();
    }

    /** 生成一句话诊断（最薄弱知识点）。供学生报告与教师看板复用。 */
    @Override
    public String summarizeWeakPoints(List<WeakPoint> weakPoints) {
        List<WeakPoint> data = ServiceSupport.nullToEmpty(weakPoints);
        if (data.isEmpty()) {
            return "暂无错题！";
        }
        // 入参已按权重降序，第一条就是最薄弱的知识点
        WeakPoint first = data.get(0);
        return "最薄弱知识点：" + first.errorType() + "（出现 " + first.count() + " 次）";
    }

    private ErrorRecordStore store() {
        return ServiceSupport.required(errorRecordStoreProvider, "ErrorRecordStore");
    }

    private SourceFileStore sourceFileStore() {
        return ServiceSupport.required(sourceFileStoreProvider, "SourceFileStore");
    }
}
