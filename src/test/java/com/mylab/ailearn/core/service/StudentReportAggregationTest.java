package com.mylab.ailearn.core.service;

import com.mylab.ailearn.core.enums.CourseChapter;
import com.mylab.ailearn.core.enums.ErrorCategory;
import com.mylab.ailearn.core.enums.ProgrammingLanguage;
import com.mylab.ailearn.core.model.commonmodel.ErrorRecord;
import com.mylab.ailearn.core.model.commonmodel.SourceFile;
import com.mylab.ailearn.core.model.specialmodel.StudentDashboard;
import com.mylab.ailearn.core.service.spi.ErrorRecordStore;
import com.mylab.ailearn.core.service.spi.SourceFileStore;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 学生报告的聚合边界：走纯函数重载，用桩端口构造服务，不依赖 Mockito 与数据库。
 */
class StudentReportAggregationTest {

    private final StudentReportServiceImpl service = new StudentReportServiceImpl(
            StubObjectProvider.<ErrorRecordStore>of(null),
            StubObjectProvider.<SourceFileStore>of(null));

    @Test
    void ignoresSubmissionsWithoutIdWhenComputingAccuracy() {
        // 没有 id 的提交无法与错题关联、也无从判断对错，不应计入正确率的分母
        List<SourceFile> submissions = List.of(
                new SourceFile(null, 1L, "a.cpp", ProgrammingLanguage.CPP, null, "int main(){}", LocalDateTime.now()),
                new SourceFile(5L, 1L, "b.cpp", ProgrammingLanguage.CPP, null, "int main(){}", LocalDateTime.now()));

        StudentDashboard dashboard = service.buildStudentDashboard(List.of(), submissions);

        assertThat(dashboard.accuracyRate()).isEqualTo(1.0);
        assertThat(dashboard.totalSubmissions()).isEqualTo(2);
    }

    @Test
    void returnsEmptyAccuracyWhenNoSubmissionIsJudgeable() {
        List<SourceFile> submissions = List.of(
                new SourceFile(null, 1L, "a.cpp", ProgrammingLanguage.CPP, null, "int main(){}", LocalDateTime.now()));

        assertThat(service.buildStudentDashboard(List.of(), submissions).accuracyRate()).isEqualTo(0.0);
    }

    @Test
    void returnsEmptyWeakPointsForNonPositiveLimit() {
        List<ErrorRecord> records = List.of(
                new ErrorRecord(null, 1L, null, CourseChapter.ARRAYS, ErrorCategory.LOGIC_ERROR,
                        "数组越界访问", "E001", "建议", List.of(), LocalDateTime.parse("2026-01-05T10:00:00"), false));

        assertThat(service.aggregateWeakPoints(records, 0)).isEmpty();
        assertThat(service.aggregateWeakPoints(records, -1)).isEmpty();
    }
}
