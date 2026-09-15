package com.mylab.ailearn.core.controller;

import com.mylab.ailearn.base.global.configs.CurrentUserResolver;
import com.mylab.ailearn.core.model.commonmodel.AiChatRequest;
import com.mylab.ailearn.core.service.AiAssistant;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.util.UUID;

/*
* Ai学习对话助手
* */
@RestController
@RequestMapping("/core/assistant")
@RequiredArgsConstructor
@Tag(name = "AI 核心 - 学习助手",
        description = "AI 学习助手，基于 Spring AI 提供与批改系统的对话问答能力，支持多轮会话记忆。")
public class AiAssistantController {
        private final CurrentUserResolver currentUserResolver;
        private final AiAssistant aiAssistant;
        @PostMapping(value = "/chat", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
        @Operation(summary = "AI 学习助手对话", description = "流式返回 AI 回答（SSE），响应头 X-Conversation-Id 返回会话 ID，前端后续对话原样传回")
        @SecurityRequirement(name = "bearerAuth")
        public ResponseEntity<Flux<String>> aiCommunicate(@Valid @RequestBody AiChatRequest request){
                Long ownerUserId = currentUserResolver.currentUserId();
                String conversationId;
                if(request.conversationId()==null||request.conversationId().isBlank()){
                        // 去掉连字符的 32 位 UUID：队友 Service 会拼 "用户ID_" 前缀，
                        // 必须保证 总长度 <= 记忆表 conversation_id 的 varchar(36)
                        conversationId = UUID.randomUUID().toString().replace("-", "");
                }
                else {
                         conversationId=request.conversationId();
                }
                Flux<String> stream=aiAssistant.aiChat(request.message(), ownerUserId, conversationId);
                HttpHeaders headers = new HttpHeaders();
                headers.set("X-Conversation-Id", conversationId);
                return ResponseEntity.ok().headers(headers).body(stream);
        }
}
