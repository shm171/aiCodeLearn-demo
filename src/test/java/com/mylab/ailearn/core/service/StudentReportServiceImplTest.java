package com.mylab.ailearn.core.service;

import com.mylab.ailearn.core.enums.CourseChapter;
import com.mylab.ailearn.core.enums.ErrorCategory;
import com.mylab.ailearn.core.enums.ProgrammingLanguage;
import com.mylab.ailearn.core.model.commonmodel.ErrorRecord;
import com.mylab.ailearn.core.model.commonmodel.SourceFile;
import com.mylab.ailearn.core.model.specialmodel.ErrorDistribution;
import com.mylab.ailearn.core.model.specialmodel.LearningCurve;
import com.mylab.ailearn.core.model.specialmodel.MonthlyPoint;
import com.mylab.ailearn.core.model.specialmodel.PieSlice;
import com.mylab.ailearn.core.model.specialmodel.StudentDashboard;
import com.mylab.ailearn.core.model.specialmodel.WeakPointReport;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

/**
 * 学生学习报告的聚合逻辑单测：走纯函数重载，持久化端口用 Mockito 桩占位（不会真正查库）。
 */
class StudentReportServiceImplTest {

    private final StudentReportServiceImpl service = new StudentReportServiceImpl(mock(ObjectProvider.class), mock(ObjectProvider.class));

    @Test
    void buildsErrorDistributionByCategory() {
        List<ErrorRecord> records = List.of(
                rec(1L, CourseChapter.ARRAYS, ErrorCategory.FORMAT_ERROR, "缩进不一致", at("2026-01-05T10:00:00")),
                rec(1L, CourseChapter.ARRAYS, ErrorCategory.FORMAT_ERROR, "变量命名不规范", at("2026-01-06T10:00:00")),
                rec(1L, CourseChapter.ARRAYS, ErrorCategory.FORMAT_ERROR, "缩进不一致", at("2026-01-07T10:00:00")),
                rec(1L, CourseChapter.POINTERS, ErrorCategory.SYNTAX_ERROR, "花括号不匹配", at("2026-01-08T10:00:00")),
                rec(1L, CourseChapter.POINTERS, ErrorCategory.SYNTAX_ERROR, "圆括号不匹配", at("2026-01-09T10:00:00")),
                rec(1L, CourseChapter.ARRAYS, ErrorCategory.LOGIC_ERROR, "数组越界访问", at("2026-01-10T10:00:00")));

        ErrorDistribution d = service.buildErrorDistribution(records);

        assertThat(d.total()).isEqualTo(6);
        assertThat(d.slices()).extracting(PieSlice::label)
                .containsExactly("格式错误", "语法错误", "逻辑错误");
        assertThat(d.slices()).extracting(PieSlice::value).containsExactly(3L, 2L, 1L);
    }

    @Test
    void buildsWeakPointReportWithRanking() {
        List<ErrorRecord> records = List.of(
                rec(1L, CourseChapter.ARRAYS, ErrorCategory.LOGIC_ERROR, "数组越界访问", at("2026-01-05T10:00:00")),
                rec(1L, CourseChapter.ARRAYS, ErrorCategory.LOGIC_ERROR, "数组越界访问", at("2026-01-06T10:00:00")),
                rec(1L, CourseChapter.POINTERS, ErrorCategory.LOGIC_ERROR, "疑似内存泄漏", at("2026-01-07T10:00:00")));

        WeakPointReport r = service.buildWeakPointReport(records);

        assertThat(r.weakPoints()).isNotEmpty();
        assertThat(r.weakPoints().get(0).errorType()).isEqualTo("数组越界访问");
        assertThat(r.weakPoints().get(0).count()).isEqualTo(2);
        assertThat(r.summary()).contains("数组越界访问");
        assertThat(r.practiceList()).isNotEmpty();
    }

    @Test
    void buildsPracticeListSortedByFrequencyAndTime() {
        List<ErrorRecord> records = List.of(
                rec(1L, CourseChapter.ARRAYS, ErrorCategory.LOGIC_ERROR, "数组越界访问", at("2026-01-05T10:00:00")),
                rec(1L, CourseChapter.ARRAYS, ErrorCategory.LOGIC_ERROR, "数组越界访问", at("2026-01-06T10:00:00")),
                rec(1L, CourseChapter.POINTERS, ErrorCategory.LOGIC_ERROR, "疑似内存泄漏", at("2026-01-07T10:00:00")));

        List<ErrorRecord> list = service.buildPracticeList(records);

        assertThat(list).hasSize(3);
        // 数组越界出现 2 次，频率最高，排前面；同频率内按归档时间倒序
        assertThat(list).extracting(ErrorRecord::errorType)
                .containsExactly("数组越界访问", "数组越界访问", "疑似内存泄漏");
        assertThat(list.get(0).createdAt()).isEqualTo(at("2026-01-06T10:00:00"));
    }

    @Test
    void computesMasteryAndAccuracy() {
        List<SourceFile> submissions = List.of(
                new SourceFile(1L, 1L, "a.cpp", ProgrammingLanguage.CPP, null, "int main(){}", LocalDateTime.now()),
                new SourceFile(2L, 1L, "b.cpp", ProgrammingLanguage.CPP, null, "int main(){}", LocalDateTime.now()));
        List<ErrorRecord> records = List.of(
                recMastered(1L, 1L, CourseChapter.ARRAYS, ErrorCategory.LOGIC_ERROR, "数组越界访问", at("2026-01-05T10:00:00"), true),
                recMastered(1L, 1L, CourseChapter.ARRAYS, ErrorCategory.LOGIC_ERROR, "数组越界访问", at("2026-01-06T10:00:00"), false));

        StudentDashboard d = service.buildStudentDashboard(records, submissions);

        assertThat(d.totalSubmissions()).isEqualTo(2);
        assertThat(d.accuracyRate()).isEqualTo(0.5);
        assertThat(d.topWeakPoints()).singleElement().satisfies(w -> {
            assertThat(w.count()).isEqualTo(2);
            assertThat(w.mastery()).isEqualTo(0.5);
        });
    }

    @Test
    void groupsLearningCurveByMonth() {
        List<ErrorRecord> records = List.of(
                rec(1L, CourseChapter.ARRAYS, ErrorCategory.LOGIC_ERROR, "数组越界访问", at("2026-01-05T10:00:00")),
                rec(1L, CourseChapter.ARRAYS, ErrorCategory.LOGIC_ERROR, "数组越界访问", at("2026-01-20T10:00:00")),
                rec(1L, CourseChapter.ARRAYS, ErrorCategory.LOGIC_ERROR, "数组越界访问", at("2026-02-03T10:00:00")));

        LearningCurve curve = service.buildLearningCurve(records);

        assertThat(curve.points()).hasSize(2);
        MonthlyPoint first = curve.points().get(0);
        assertThat(first.year()).isEqualTo(2026);
        assertThat(first.month()).isEqualTo(1);
        assertThat(first.errorCount()).isEqualTo(2);
        MonthlyPoint second = curve.points().get(1);
        assertThat(second.month()).isEqualTo(2);
        assertThat(second.errorCount()).isEqualTo(1);
    }

    private ErrorRecord rec(Long owner, CourseChapter chapter, ErrorCategory category, String errorType, LocalDateTime at) {
        return new ErrorRecord(null, owner, null, chapter, category, errorType, "E001", "修复建议", List.of(), at, false);
    }

    private ErrorRecord recMastered(Long owner, Long sourceFileId, CourseChapter chapter, ErrorCategory category,
                                    String errorType, LocalDateTime at, boolean mastered) {
        return new ErrorRecord(null, owner, sourceFileId, chapter, category, errorType, "E001", "修复建议", List.of(), at, mastered);
    }

    private LocalDateTime at(String text) {
        return LocalDateTime.parse(text);
    }
}
