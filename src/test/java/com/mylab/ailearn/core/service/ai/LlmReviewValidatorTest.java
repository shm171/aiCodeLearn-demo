package com.mylab.ailearn.core.service.ai;

import com.mylab.ailearn.core.enums.CourseChapter;
import com.mylab.ailearn.core.enums.ErrorCategory;
import com.mylab.ailearn.core.enums.ProgrammingLanguage;
import com.mylab.ailearn.core.model.commonmodel.GradedError;
import com.mylab.ailearn.core.model.commonmodel.LlmReview;
import com.mylab.ailearn.core.model.commonmodel.LlmReviewStatus;
import com.mylab.ailearn.core.model.commonmodel.SourceFile;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * LLM 输出校验的行为约束：结构合法但语义非法的模型结果不得进入分数与错题归档。
 */
class LlmReviewValidatorTest {

    private static final String SOURCE = """
            public class Main {
                public static void main(String[] args) {
                    int a = 1;
                }
            }
            """;

    private final LlmReviewValidator validator = new LlmReviewValidator();

    @Test
    void acceptsFullyValidReview() {
        LlmReview raw = reviewOf(List.of(issue(ErrorCategory.LOGIC_ERROR, "数组越界访问",
                "ARRAY_OUT_OF_BOUNDS", List.of(3))), "整体思路不错");

        LlmReview result = validator.validate(raw, source());

        assertThat(result.status()).isEqualTo(LlmReviewStatus.COMPLETED);
        assertThat(result.issues()).hasSize(1);
    }

    @Test
    void rejectsIssueWithoutCategory() {
        LlmReview raw = reviewOf(List.of(issue(null, "数组越界访问",
                "ARRAY_OUT_OF_BOUNDS", List.of(3))), "模型结论");

        LlmReview result = validator.validate(raw, source());

        assertThat(result.issues()).isEmpty();
        assertThat(result.status()).isEqualTo(LlmReviewStatus.UNAVAILABLE);
    }

    @Test
    void rejectsIssueWithLineBeyondSourceRange() {
        LlmReview raw = reviewOf(List.of(issue(ErrorCategory.LOGIC_ERROR, "数组越界访问",
                "ARRAY_OUT_OF_BOUNDS", List.of(9999))), "模型结论");

        LlmReview result = validator.validate(raw, source());

        assertThat(result.issues()).isEmpty();
        assertThat(result.status()).isEqualTo(LlmReviewStatus.UNAVAILABLE);
    }

    @Test
    void rejectsIssueWithNonPositiveLine() {
        LlmReview raw = reviewOf(List.of(issue(ErrorCategory.LOGIC_ERROR, "数组越界访问",
                "ARRAY_OUT_OF_BOUNDS", List.of(0, -1))), "模型结论");

        LlmReview result = validator.validate(raw, source());

        assertThat(result.issues()).isEmpty();
        assertThat(result.status()).isEqualTo(LlmReviewStatus.UNAVAILABLE);
    }

    @Test
    void keepsValidIssuesAndDropsInvalidOnes() {
        LlmReview raw = reviewOf(List.of(
                issue(ErrorCategory.LOGIC_ERROR, "数组越界访问", "ARRAY_OUT_OF_BOUNDS", List.of(3)),
                issue(ErrorCategory.LOGIC_ERROR, "越界行号", "ARRAY_OUT_OF_BOUNDS", List.of(9999))),
                "模型结论");

        LlmReview result = validator.validate(raw, source());

        assertThat(result.status()).isEqualTo(LlmReviewStatus.PARTIAL);
        assertThat(result.issues()).hasSize(1);
        assertThat(result.issues().get(0).line()).containsExactly(3);
    }

    @Test
    void rejectsReviewExceedingIssueBudget() {
        List<GradedError> tooMany = new ArrayList<>();
        for (int i = 0; i < LlmReviewValidator.MAX_ISSUES + 1; i++) {
            tooMany.add(issue(ErrorCategory.LOGIC_ERROR, "数组越界访问", "ARRAY_OUT_OF_BOUNDS", List.of(3)));
        }

        LlmReview result = validator.validate(reviewOf(tooMany, "模型结论"), source());

        assertThat(result.issues()).isEmpty();
        assertThat(result.status()).isEqualTo(LlmReviewStatus.UNAVAILABLE);
    }

    @Test
    void rejectsIssueExceedingFieldLengthBudget() {
        String oversized = "越界".repeat(LlmReviewValidator.MAX_MESSAGE_LENGTH);
        LlmReview raw = reviewOf(List.of(new GradedError(
                ErrorCategory.LOGIC_ERROR, "数组越界访问", "ARRAY_OUT_OF_BOUNDS",
                oversized, "改掉", "LLM 深度批改", List.of(3))), "模型结论");

        LlmReview result = validator.validate(raw, source());

        assertThat(result.issues()).isEmpty();
        assertThat(result.status()).isEqualTo(LlmReviewStatus.UNAVAILABLE);
    }

    @Test
    void rejectsIssueWithMissingErrorCode() {
        LlmReview raw = reviewOf(List.of(issue(ErrorCategory.LOGIC_ERROR, "数组越界访问", "  ", List.of(3))),
                "模型结论");

        LlmReview result = validator.validate(raw, source());

        assertThat(result.issues()).isEmpty();
        assertThat(result.status()).isEqualTo(LlmReviewStatus.UNAVAILABLE);
    }

    @Test
    void rejectsKnownCodeWithContradictingCategory() {
        // 已知规则代码却声明成更轻的分类，等于用错分类降低扣分权重
        LlmReview raw = reviewOf(List.of(issue(ErrorCategory.FORMAT_ERROR, "数组越界访问",
                "ARRAY_OUT_OF_BOUNDS", List.of(3))), "模型结论");

        LlmReview result = validator.validate(raw, source());

        assertThat(result.issues()).isEmpty();
        assertThat(result.status()).isEqualTo(LlmReviewStatus.UNAVAILABLE);
    }

    @Test
    void truncatesOverlongSummary() {
        LlmReview raw = reviewOf(List.of(), "很长".repeat(LlmReviewValidator.MAX_SUMMARY_LENGTH));

        LlmReview result = validator.validate(raw, source());

        assertThat(result.status()).isEqualTo(LlmReviewStatus.COMPLETED);
        assertThat(result.summary()).hasSize(LlmReviewValidator.MAX_SUMMARY_LENGTH);
    }

    @Test
    void treatsMissingReviewAsUntrusted() {
        LlmReview result = validator.validate(null, source());

        assertThat(result.issues()).isEmpty();
        assertThat(result.status()).isEqualTo(LlmReviewStatus.UNAVAILABLE);
    }

    @Test
    void validatesRawUnverifiedReview() {
        // 模型原始输出（状态未填 = 未经校验）校验通过后才是 COMPLETED
        LlmReview raw = new LlmReview(List.of(issue(ErrorCategory.LOGIC_ERROR, "数组越界访问",
                "ARRAY_OUT_OF_BOUNDS", List.of(3))), "模型结论", null);

        assertThat(raw.status()).isEqualTo(LlmReviewStatus.UNVERIFIED);

        LlmReview result = validator.validate(raw, source());

        assertThat(result.status()).isEqualTo(LlmReviewStatus.COMPLETED);
        assertThat(result.issues()).hasSize(1);
    }

    @Test
    void keepsClientDeclaredUnavailableInsteadOfUpgradingIt() {
        // 模型未接入/调用失败的空结论不能被当成「没有发现问题」
        LlmReview result = validator.validate(LlmReview.unavailable("LLM 调用失败"), source());

        assertThat(result.issues()).isEmpty();
        assertThat(result.status()).isEqualTo(LlmReviewStatus.UNAVAILABLE);
    }

    @Test
    void emptyIssueListIsCompleteNotUnavailable() {
        LlmReview result = validator.validate(reviewOf(List.of(), "没有发现问题"), source());

        assertThat(result.issues()).isEmpty();
        assertThat(result.status()).isEqualTo(LlmReviewStatus.COMPLETED);
    }

    @Test
    void rejectsLineBeyondShorterSource() {
        SourceFile singleLine = new SourceFile(null, 1L, "Main.java", ProgrammingLanguage.JAVA,
                CourseChapter.COMPREHENSIVE, "int a = 1;", LocalDateTime.now());
        LlmReview raw = reviewOf(List.of(issue(ErrorCategory.LOGIC_ERROR, "越界", "ARRAY_OUT_OF_BOUNDS",
                List.of(2))), "模型结论");

        LlmReview result = validator.validate(raw, singleLine);

        assertThat(result.issues()).isEmpty();
        assertThat(result.status()).isEqualTo(LlmReviewStatus.UNAVAILABLE);
    }

    private SourceFile source() {
        return new SourceFile(null, 1L, "Main.java", ProgrammingLanguage.JAVA,
                CourseChapter.COMPREHENSIVE, SOURCE, LocalDateTime.now());
    }

    private LlmReview reviewOf(List<GradedError> issues, String summary) {
        return new LlmReview(issues, summary, LlmReviewStatus.COMPLETED);
    }

    private GradedError issue(ErrorCategory category, String errorType, String errorCode, List<Integer> lines) {
        return new GradedError(category, errorType, errorCode, "说明", "建议", "LLM 深度批改", lines);
    }
}
