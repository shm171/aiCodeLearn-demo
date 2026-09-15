package com.mylab.ailearn.core.service;

import com.mylab.ailearn.core.enums.CourseChapter;
import com.mylab.ailearn.core.enums.ErrorCategory;
import com.mylab.ailearn.core.enums.ProgrammingLanguage;
import com.mylab.ailearn.core.enums.RuleType;
import com.mylab.ailearn.core.model.commonmodel.GradedError;
import com.mylab.ailearn.core.model.commonmodel.GradingResult;
import com.mylab.ailearn.core.model.commonmodel.LlmReview;
import com.mylab.ailearn.core.model.commonmodel.LlmReviewStatus;
import com.mylab.ailearn.core.model.commonmodel.RuleViolation;
import com.mylab.ailearn.core.model.commonmodel.SourceFile;
import com.mylab.ailearn.core.model.commonmodel.StaticCheckReport;
import com.mylab.ailearn.core.service.ai.LlmReviewValidator;
import com.mylab.ailearn.core.service.spi.LlmGradingClient;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 模型输出进入分数与错题归档前的落地校验（S09 验收）。
 *
 * <p>用真实 {@link GradingServiceImpl} + 桩客户端，覆盖「Mock 返回 null 分类、
 * 越界行号、超量 issues」三类非法输出。</p>
 */
class GradingServiceImplSecurityTest {

    private static final String SOURCE = """
            public class Main {
                public static void main(String[] args) {
                    int a = 1;
                }
            }
            """;

    private final LlmReviewValidator validator = new LlmReviewValidator();

    @Test
    void dropsLlmIssueWithoutCategory() {
        LlmGradingClient client = file -> new LlmReview(
                List.of(llmIssue(null, List.of(3))), "模型结论", LlmReviewStatus.COMPLETED);

        GradingResult result = serviceWith(noViolations(), client).grade(source());

        assertThat(result.errors()).noneMatch(e -> GradingServiceImpl.SOURCE_LLM.equals(e.source()));
        assertThat(result.feedback()).contains("未产出可信结论");
    }

    @Test
    void dropsLlmIssueWithOutOfRangeLine() {
        LlmGradingClient client = file -> new LlmReview(
                List.of(llmIssue(ErrorCategory.LOGIC_ERROR, List.of(50000))), "模型结论",
                LlmReviewStatus.COMPLETED);

        GradingResult result = serviceWith(noViolations(), client).grade(source());

        assertThat(result.errors()).noneMatch(e -> GradingServiceImpl.SOURCE_LLM.equals(e.source()));
        assertThat(result.feedback()).contains("未产出可信结论");
    }

    @Test
    void rejectsOverLimitLlmOutput() {
        List<GradedError> tooMany = new ArrayList<>();
        for (int i = 0; i < LlmReviewValidator.MAX_ISSUES + 1; i++) {
            tooMany.add(llmIssue(ErrorCategory.LOGIC_ERROR, List.of(3)));
        }
        LlmGradingClient client = file -> new LlmReview(tooMany, "模型结论", LlmReviewStatus.COMPLETED);

        GradingResult result = serviceWith(noViolations(), client).grade(source());

        assertThat(result.errors()).noneMatch(e -> GradingServiceImpl.SOURCE_LLM.equals(e.source()));
        assertThat(result.feedback()).contains("未产出可信结论");
    }

    @Test
    void keepsValidLlmIssuesAndFlagsIncompleteResult() {
        LlmGradingClient client = file -> new LlmReview(List.of(
                llmIssue(ErrorCategory.LOGIC_ERROR, List.of(3)),
                llmIssue(ErrorCategory.LOGIC_ERROR, List.of(9999))), "模型结论", LlmReviewStatus.COMPLETED);

        GradingResult result = serviceWith(noViolations(), client).grade(source());

        assertThat(result.errors()).hasSize(1);
        assertThat(result.errors().get(0).source()).isEqualTo(GradingServiceImpl.SOURCE_LLM);
        assertThat(result.feedback()).contains("不完整");
    }

    @Test
    void validLlmIssuesEnterScore() {
        LlmGradingClient client = file -> new LlmReview(
                List.of(llmIssue(ErrorCategory.LOGIC_ERROR, List.of(3))), "整体思路欠佳",
                LlmReviewStatus.COMPLETED);

        GradingResult result = serviceWith(noViolations(), client).grade(source());

        assertThat(result.errors()).hasSize(1);
        assertThat(result.score()).isEqualTo(92);
        assertThat(result.feedback()).isEqualTo("整体思路欠佳");
    }

    @Test
    void unclassifiedRuleViolationIsNotFree() {
        // 无法归类的问题按兜底权重扣分，不能绕过分类逃避扣分
        StaticCheckService staticCheck = file -> new StaticCheckReport(List.of(
                new RuleViolation(RuleType.INDENTATION, null, "未分类问题", 1, "调整缩进")));

        GradingResult result = serviceWith(staticCheck, null).grade(source());

        assertThat(result.errors()).hasSize(1);
        assertThat(result.score()).isEqualTo(90);
        assertThat(result.feedback()).contains("未分类错误 1");
    }

    private GradingServiceImpl serviceWith(StaticCheckService staticCheck, LlmGradingClient client) {
        return new GradingServiceImpl(staticCheck, StubObjectProvider.of(client), validator);
    }

    private StaticCheckService noViolations() {
        return file -> new StaticCheckReport(List.of());
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
