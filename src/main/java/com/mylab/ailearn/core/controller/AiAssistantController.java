package com.mylab.ailearn.core.controller;

import com.mylab.ailearn.base.global.configs.CurrentUserResolver;
import com.mylab.ailearn.base.global.configs.OpenApiConfig;
import com.mylab.ailearn.core.model.commonmodel.AiChatRequest;
import com.mylab.ailearn.core.model.commonmodel.PracticeExercise;
import com.mylab.ailearn.core.model.commonmodel.PracticeGenerationRequest;
import com.mylab.ailearn.core.service.AiAssistant;
import com.mylab.ailearn.core.service.PracticeGenerationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.UUID;

/** AI 学习对话与类似练习生成入口。 */
@RestController
@RequestMapping("/core/assistant")
@RequiredArgsConstructor
@Tag(name = "AI 核心 - 学习助手",
        description = "AI 学习助手，支持多轮流式对话和类似练习生成。")
public class AiAssistantController {

    private static final String STREAM_DONE = "[DONE]";

    private final CurrentUserResolver currentUserResolver;
    private final AiAssistant aiAssistant;
    private final PracticeGenerationService practiceGenerationService;

    @PostMapping(value = "/chat", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @Operation(summary = "AI 学习助手对话",
            description = "流式返回 AI 回答，并在 X-Conversation-Id 响应头返回会话 ID")
    @SecurityRequirement(name = OpenApiConfig.BEARER_AUTH)
    public ResponseEntity<Flux<String>> aiCommunicate(@Valid @RequestBody AiChatRequest request) {
        Long ownerUserId = currentUserResolver.currentUserId();
        String conversationId = request.conversationId();
        if (conversationId == null || conversationId.isBlank()) {
            conversationId = UUID.randomUUID().toString().replace("-", "");
        }

        Flux<String> stream = aiAssistant.aiChat(request.message(), ownerUserId, conversationId)
                .concatWithValues(STREAM_DONE);
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Conversation-Id", conversationId);
        return ResponseEntity.ok().headers(headers).body(stream);
    }

    @PostMapping("/practice")
    @Operation(summary = "根据错题实时生成类似练习")
    @SecurityRequirement(name = OpenApiConfig.BEARER_AUTH)
    public ResponseEntity<List<PracticeExercise>> generatePractice(
            @Valid @RequestBody PracticeGenerationRequest request) {
        currentUserResolver.currentUserId();
        return ResponseEntity.ok(practiceGenerationService.generate(request));
    }
}
