package com.mylab.ailearn.core.service;

import com.mylab.ailearn.core.enums.CourseChapter;
import com.mylab.ailearn.core.enums.ErrorCategory;
import com.mylab.ailearn.core.model.commonmodel.ErrorRecord;
import com.mylab.ailearn.core.model.specialmodel.ClassDashboard;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

/**
 * 教师班级看板的聚合逻辑单测：直接传入错题数据，持久化端口用 Mockito 桩占位（不会真正查库）。
 */
class TeacherDashboardServiceImplTest {

    private final StudentReportServiceImpl studentReport = new StudentReportServiceImpl(mock(ObjectProvider.class), mock(ObjectProvider.class));
    private final TeacherDashboardServiceImpl service = new TeacherDashboardServiceImpl(mock(ObjectProvider.class), mock(ObjectProvider.class), studentReport);

    @Test
    void buildsClassDashboardSortedByStudentErrorCount() {
        List<ErrorRecord> records = List.of(
                rec(1L, CourseChapter.ARRAYS, ErrorCategory.LOGIC_ERROR, "数组越界访问", at("2026-01-05T10:00:00")),
                rec(1L, CourseChapter.ARRAYS, ErrorCategory.LOGIC_ERROR, "数组越界访问", at("2026-01-06T10:00:00")),
                rec(2L, CourseChapter.POINTERS, ErrorCategory.SYNTAX_ERROR, "花括号不匹配", at("2026-01-07T10:00:00")));

        ClassDashboard d = service.buildClassDashboard(records);

        assertThat(d.totalErrors()).isEqualTo(3);
        assertThat(d.studentStats()).hasSize(2);
        assertThat(d.studentStats().get(0).ownerUserId()).isEqualTo(1L);
        assertThat(d.studentStats().get(0).errorCount()).isEqualTo(2);
        assertThat(d.studentStats().get(1).ownerUserId()).isEqualTo(2L);
        assertThat(d.distribution().total()).isEqualTo(3);
    }

    private ErrorRecord rec(Long owner, CourseChapter chapter, ErrorCategory category, String errorType, LocalDateTime at) {
        return new ErrorRecord(null, owner, null, chapter, category, errorType, "E001", "修复建议", List.of(), at, false);
    }

    private LocalDateTime at(String text) {
        return LocalDateTime.parse(text);
    }
}
