package com.mylab.ailearn.core.service;

import com.mylab.ailearn.core.enums.ErrorCategory;
import com.mylab.ailearn.core.enums.ProgrammingLanguage;
import com.mylab.ailearn.core.model.commonmodel.GradedError;
import com.mylab.ailearn.core.model.commonmodel.GradingResult;
import com.mylab.ailearn.core.model.commonmodel.LlmReview;
import com.mylab.ailearn.core.model.commonmodel.LlmReviewStatus;
import com.mylab.ailearn.core.model.commonmodel.SourceFile;
import com.mylab.ailearn.core.service.ai.LlmReviewValidator;
import com.mylab.ailearn.core.service.spi.LlmGradingClient;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GradingServiceImplTest {

    private final StaticCheckServiceImpl staticCheck = new StaticCheckServiceImpl();

    private GradingServiceImpl newService(ObjectProvider<LlmGradingClient> llmProvider) {
        return new GradingServiceImpl(staticCheck, llmProvider, new LlmReviewValidator());
    }

    @Test
    void gradesWithRuleOnlyWhenLlmUnavailable() {
        GradingService service = newService(mock(ObjectProvider.class));

        GradingResult result = service.grade(cpp("int main() {\n    return 0;\n"));

        assertThat(result.errors()).isNotEmpty();
        assertThat(result.errors()).allMatch(e -> GradingServiceImpl.SOURCE_RULE.equals(e.source()));
        assertThat(result.score()).isEqualTo(90);
        assertThat(result.feedback()).contains("仅规则校验");
    }

    @Test
    void mergesLlmIssuesAndScores() {
        LlmGradingClient client = mock(LlmGradingClient.class);
        when(client.review(any())).thenReturn(new LlmReview(
                List.of(new GradedError(ErrorCategory.LOGIC_ERROR, "思路偏差", "L001", "思路偏差", "换一种算法", "LLM 深度批改", List.of())),
                "整体思路欠佳", LlmReviewStatus.COMPLETED));
        ObjectProvider<LlmGradingClient> provider = mock(ObjectProvider.class);
        when(provider.getIfAvailable()).thenReturn(client);
        GradingService service = newService(provider);

        GradingResult result = service.grade(cpp("int main() {\n    return 0;\n"));

        assertThat(result.errors()).hasSize(2);
        assertThat(result.score()).isEqualTo(82);
        assertThat(result.feedback()).contains("整体思路欠佳");
    }

    @Test
    void deduplicatesOverlappingRuleAndLlmErrors() {
        LlmGradingClient client = mock(LlmGradingClient.class);
        when(client.review(any())).thenReturn(new LlmReview(
                List.of(new GradedError(ErrorCategory.SYNTAX_ERROR, "花括号不匹配", "BRACE_MISMATCH", "花括号不匹配", "补右括号", "LLM 深度批改", List.of())),
                "s", LlmReviewStatus.COMPLETED));
        ObjectProvider<LlmGradingClient> provider = mock(ObjectProvider.class);
        when(provider.getIfAvailable()).thenReturn(client);
        GradingService service = newService(provider);

        GradingResult result = service.grade(cpp("int main() {\n    return 0;\n"));

        assertThat(result.errors()).hasSize(1);
        assertThat(result.score()).isEqualTo(90);
    }

    private SourceFile cpp(String content) {
        return new SourceFile(null, 1L, "main.cpp", ProgrammingLanguage.CPP, null, content, LocalDateTime.now());
    }
}
