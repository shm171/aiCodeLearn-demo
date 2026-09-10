package com.mylab.ailearn.core.service;

import com.mylab.ailearn.core.enums.CourseChapter;
import com.mylab.ailearn.core.enums.ErrorCategory;
import com.mylab.ailearn.core.enums.ProgrammingLanguage;
import com.mylab.ailearn.core.model.commonmodel.GradedError;
import com.mylab.ailearn.core.model.commonmodel.GradingResult;
import com.mylab.ailearn.core.model.commonmodel.GradingStage;
import com.mylab.ailearn.core.model.commonmodel.GradingStageOutcome;
import com.mylab.ailearn.core.model.commonmodel.GradingStageReport;
import com.mylab.ailearn.core.model.commonmodel.GradingStatus;
import com.mylab.ailearn.core.model.commonmodel.LlmReview;
import com.mylab.ailearn.core.model.commonmodel.LlmReviewStatus;
import com.mylab.ailearn.core.model.commonmodel.SourceFile;
import com.mylab.ailearn.core.model.commonmodel.StaticCheckReport;
import com.mylab.ailearn.core.service.ai.LlmReviewValidator;
import com.mylab.ailearn.core.service.spi.LlmGradingClient;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 结果完整性（S10 验收）：分数必须自带可采纳性，「没有检测结果」不能冒充「检查通过」。
 *
 * <p>关键场景：模型不可用时规则层没检出问题，分数仍是 100，但它不是可采纳的正式成绩——
 * 机器消费者要能直接读出来，而不是从一段自然语言里猜。</p>
 */
class GradingResultStatusTest {

    private static final String SOURCE = """
            public class Main {
                public static void main(String[] args) {
                    int a = 1;
                }
            }
            """;

    @Test
    void cleanScoreWithMissingLlmIsNotAdoptable() {
        GradingResult result = gradeWith(null);

        assertThat(result.errors()).isEmpty();
        assertThat(result.score()).isEqualTo(100);
        assertThat(result.status()).isEqualTo(GradingStatus.PARTIAL);
        assertThat(result.scoreAdoptable()).isFalse();
        assertThat(result.stage(GradingStage.LLM_REVIEW).outcome())
                .isEqualTo(GradingStageOutcome.NOT_CONFIGURED);
        assertThat(result.stage(GradingStage.RULE_STATIC_CHECK).outcome())
                .isEqualTo(GradingStageOutcome.COMPLETED);
    }

    @Test
    void cleanScoreWithCompleteStagesIsAdoptable() {
        // 与上一个用例同样是 100 分，但两阶段都完整完成，分数可采纳
        GradingResult result = gradeWith(file -> new LlmReview(List.of(), "没有需要补充的问题",
                LlmReviewStatus.COMPLETED));

        assertThat(result.score()).isEqualTo(100);
        assertThat(result.status()).isEqualTo(GradingStatus.COMPLETED);
        assertThat(result.scoreAdoptable()).isTrue();
        assertThat(result.stage(GradingStage.LLM_REVIEW).outcome())
                .isEqualTo(GradingStageOutcome.COMPLETED);
    }

    @Test
    void failedLlmCallIsReportedAsUnavailable() {
        GradingResult result = gradeWith(file -> LlmReview.unavailable("LLM 调用失败，本次仅规则校验。"));

        assertThat(result.status()).isEqualTo(GradingStatus.UNAVAILABLE);
        assertThat(result.scoreAdoptable()).isFalse();
        assertThat(result.stage(GradingStage.LLM_REVIEW).outcome())
                .isEqualTo(GradingStageOutcome.UNAVAILABLE);
    }

    @Test
    void partiallyValidLlmOutputIsReportedAsPartial() {
        GradingResult result = gradeWith(file -> new LlmReview(List.of(
                llmIssue(ErrorCategory.LOGIC_ERROR, List.of(3)),
                llmIssue(ErrorCategory.LOGIC_ERROR, List.of(9999))
        ), "模型结论", LlmReviewStatus.COMPLETED));

        assertThat(result.errors()).hasSize(1);
        assertThat(result.status()).isEqualTo(GradingStatus.PARTIAL);
        assertThat(result.scoreAdoptable()).isFalse();
        assertThat(result.stage(GradingStage.LLM_REVIEW).outcome())
                .isEqualTo(GradingStageOutcome.PARTIAL);
    }

    @Test
    void allStagesAreReported() {
        GradingResult result = gradeWith(file -> new LlmReview(List.of(), "结论", LlmReviewStatus.COMPLETED));

        assertThat(result.stages()).hasSize(2);
        assertThat(result.stages()).extracting(GradingStageReport::stage)
                .containsExactly(GradingStage.RULE_STATIC_CHECK, GradingStage.LLM_REVIEW);
    }

    @Test
    void resultWithoutStatusIsNotAdoptable() {
        // 直接构造时缺状态/缺阶段：失败关闭，绝不默认可信
        GradingResult result = new GradingResult(List.of(), 100, "无", null, null);

        assertThat(result.status()).isEqualTo(GradingStatus.UNAVAILABLE);
        assertThat(result.scoreAdoptable()).isFalse();
        assertThat(result.stages()).isEmpty();
    }

    @Test
    void adoptableRequiresEveryStageCompleted() {
        // 整体状态与阶段状态不一致时，以阶段为准（失败关闭）
        GradingResult inconsistent = new GradingResult(List.of(), 100, "无", GradingStatus.COMPLETED,
                List.of(new GradingStageReport(
                        GradingStage.LLM_REVIEW, GradingStageOutcome.UNAVAILABLE, "模型未产出结论")));

        assertThat(inconsistent.scoreAdoptable()).isFalse();
    }

    private GradingResult gradeWith(LlmGradingClient client) {
        GradingServiceImpl service = new GradingServiceImpl(
                file -> new StaticCheckReport(List.of()),
                StubObjectProvider.of(client),
                new LlmReviewValidator());

        return service.grade(source());
    }

    private GradedError llmIssue(ErrorCategory category, List<Integer> lines) {
        return new GradedError(category, "数组越界访问", "ARRAY_OUT_OF_BOUNDS",
                "循环上界用错", "改为 < n", "LLM 深度批改", lines);
    }

    private SourceFile source() {
        return new SourceFile(null, 1L, "Main.java", ProgrammingLanguage.JAVA,
                CourseChapter.COMPREHENSIVE, SOURCE, LocalDateTime.now());
    }
}
