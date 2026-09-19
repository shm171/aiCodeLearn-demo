package com.mylab.ailearn.core.controller;

import com.mylab.ailearn.base.global.configs.CurrentUserResolver;
import com.mylab.ailearn.core.model.commonmodel.AiChatRequest;
import com.mylab.ailearn.core.service.AiAssistant;
import com.mylab.ailearn.core.service.PracticeGenerationService;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AiAssistantControllerTest {

    @Test
    void appendsProtocolDoneEventAfterAssistantContent() {
        CurrentUserResolver currentUserResolver = mock(CurrentUserResolver.class);
        AiAssistant aiAssistant = mock(AiAssistant.class);
        PracticeGenerationService practiceGenerationService = mock(PracticeGenerationService.class);
        AiAssistantController controller = new AiAssistantController(
                currentUserResolver, aiAssistant, practiceGenerationService);

        when(currentUserResolver.currentUserId()).thenReturn(7L);
        when(aiAssistant.aiChat("继续讲解", 7L, "conversation-1"))
                .thenReturn(Flux.just("第一段", "第二段"));

        ResponseEntity<Flux<String>> response = controller.aiCommunicate(
                new AiChatRequest("继续讲解", "conversation-1"));
        List<String> events = response.getBody()
                .collectList()
                .block(Duration.ofSeconds(1));

        assertThat(response.getHeaders().getFirst("X-Conversation-Id"))
                .isEqualTo("conversation-1");
        assertThat(events).containsExactly("第一段", "第二段", "[DONE]");
    }
}
