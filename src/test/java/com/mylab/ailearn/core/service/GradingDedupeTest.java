package com.mylab.ailearn.core.service;

import com.mylab.ailearn.core.enums.CourseChapter;
import com.mylab.ailearn.core.enums.ErrorCategory;
import com.mylab.ailearn.core.enums.ProgrammingLanguage;
import com.mylab.ailearn.core.model.commonmodel.GradedError;
import com.mylab.ailearn.core.model.commonmodel.GradingResult;
import com.mylab.ailearn.core.model.commonmodel.LlmReview;
import com.mylab.ailearn.core.model.commonmodel.LlmReviewStatus;
import com.mylab.ailearn.core.model.commonmodel.RuleViolation;
import com.mylab.ailearn.core.model.commonmodel.SourceFile;
import com.mylab.ailearn.core.model.commonmodel.StaticCheckReport;
import com.mylab.ailearn.core.enums.RuleType;
import com.mylab.ailearn.core.service.ai.LlmReviewValidator;
import com.mylab.ailearn.core.service.spi.LlmGradingClient;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 错误清单去重的行为约束：同一类错误只保留一条，但必须把行号并起来，
 * 不能因为去重让学生漏看其它出错位置。
 */
class GradingDedupeTest {

    private static final String SOURCE = """
            int main() {
                int a = 1;
                int b = 2;
                int c = 3;
                int d = 4;
                return 0;
            }
            """;

    @Test
    void mergesLinesOfSameErrorTypeFromLlm() {
        GradingResult result = grade(noViolations(), llmWith(
                llmIssue(ErrorCategory.LOGIC_ERROR, "数组越界访问", "ARRAY_OUT_OF_BOUNDS", 3),
                llmIssue(ErrorCategory.LOGIC_ERROR, "数组越界访问", "ARRAY_OUT_OF_BOUNDS", 5)));

        assertThat(result.errors()).hasSize(1);
        assertThat(result.errors().get(0).line()).containsExactly(3, 5);
    }

    @Test
    void mergesRuleAndLlmFindingsOfSameType() {
        StaticCheckService staticCheck = file -> new StaticCheckReport(List.of(
                new RuleViolation(RuleType.ARRAY_OUT_OF_BOUNDS, ErrorCategory.LOGIC_ERROR,
                        "数组下标越界", 2, "检查下标范围")));

        GradingResult result = grade(staticCheck, llmWith(
                llmIssue(ErrorCategory.LOGIC_ERROR, "数组越界访问", "ARRAY_OUT_OF_BOUNDS", 5)));

        assertThat(result.errors()).hasSize(1);
        assertThat(result.errors().get(0).source()).isEqualTo(GradingServiceImpl.SOURCE_RULE);
        assertThat(result.errors().get(0).line()).containsExactly(2, 5);
    }

    @Test
    void keepsDistinctErrorCodesApart() {
        GradingResult result = grade(noViolations(), llmWith(
                llmIssue(ErrorCategory.LOGIC_ERROR, "数组越界访问", "ARRAY_OUT_OF_BOUNDS", 3),
                llmIssue(ErrorCategory.LOGIC_ERROR, "疑似死循环", "INFINITE_LOOP", 5)));

        assertThat(result.errors()).hasSize(2);
    }

    @Test
    void doesNotDuplicateLinesAlreadyPresent() {
        GradingResult result = grade(noViolations(), llmWith(
                llmIssue(ErrorCategory.LOGIC_ERROR, "数组越界访问", "ARRAY_OUT_OF_BOUNDS", 3),
                llmIssue(ErrorCategory.LOGIC_ERROR, "数组越界访问", "ARRAY_OUT_OF_BOUNDS", 3)));

        assertThat(result.errors()).hasSize(1);
        assertThat(result.errors().get(0).line()).containsExactly(3);
    }

    private GradingResult grade(StaticCheckService staticCheck, LlmGradingClient client) {
        GradingServiceImpl service = new GradingServiceImpl(
                staticCheck, StubObjectProvider.of(client), new LlmReviewValidator());
        return service.grade(source());
    }

    private StaticCheckService noViolations() {
        return file -> new StaticCheckReport(List.of());
    }

    private LlmGradingClient llmWith(GradedError... issues) {
        return file -> new LlmReview(List.of(issues), "模型结论", LlmReviewStatus.COMPLETED);
    }

    private GradedError llmIssue(ErrorCategory category, String errorType, String errorCode, int line) {
        return new GradedError(category, errorType, errorCode, "说明", "建议", "LLM 深度批改", List.of(line));
    }

    private SourceFile source() {
        return new SourceFile(null, 1L, "Main.java", ProgrammingLanguage.JAVA,
                CourseChapter.COMPREHENSIVE, SOURCE, LocalDateTime.now());
    }
}
