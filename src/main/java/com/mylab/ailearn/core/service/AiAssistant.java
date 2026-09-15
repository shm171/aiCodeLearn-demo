package com.mylab.ailearn.core.service;


import com.mylab.ailearn.core.service.ai.tool.LlmErrorRecordGet;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

/**
 * AI 学习助手：基于 Spring AI {@link ChatClient} 提供与批改系统的对话问答能力。
 *
 * <p>助手可以调用 {@link LlmErrorRecordGet} 工具，按用户 ID 从数据库调取批改记录（错题），
 * 用于总结薄弱点、推荐训练章节与讲解易错知识点。对话记忆通过
 * {@link ChatMemory#CONVERSATION_ID} 按「用户 + 会话」隔离，避免不同会话串扰。</p>
 *
 * <p><b>安全边界（重要）</b>：{@link #aiChat} 把 {@code ownerUserId} 写进系统提示词，
 * 这只是给模型的上下文，<b>不构成任何数据权限</b>——真正查库时用的是模型自己生成的
 * 工具调用参数。要做数据隔离，必须在 {@link LlmErrorRecordGet} 侧改用服务端可信身份，
 * 而不是模型给出的 ID（当前尚未实现）。</p>
 */
@Service
public class AiAssistant {

    private final ChatClient chatClient;
    private final LlmErrorRecordGet llmErrorRecordGet;
    private final String systemPrompt;

    public AiAssistant(ChatClient chatClient, LlmErrorRecordGet llmErrorRecordGet) {
        this.chatClient = chatClient;
        this.llmErrorRecordGet = llmErrorRecordGet;
        this.systemPrompt = "你是一个关于批改Java和C++代码批改系统的ai助手" +
                "，你的任务主要是与用户交流，" +
                "回答用户的问题，" +
                "满足用户的要求，" +
                "包括，针对用户的角色" +
                "从数据库调取批改记录，" +
                "对批改记录的总结薄弱点，" +
                "推荐训练章节，" +
                "对易错知识点的讲解等等，" +
                "对于用户超出计算机范围的问题，不予回答" +
                "表达尽量简洁，不要重复问问题";
    }

    /**
     * 流式 AI 对话：逐段返回助手回答，供 Controller 以 text/event-stream 输出。
     *
     * @param message        用户输入的消息
     * @param ownerUserId    当前用户 ID，仅作为提示词上下文交给模型，不构成数据权限（见类注释）
     * @param conversationId 会话 ID，用于隔离不同对话的记忆（同一用户不同会话互不串扰）
     * @return 流式回答内容
     */
    public Flux<String> aiChat(String message, Long ownerUserId, String conversationId) {
        String userId = String.valueOf(ownerUserId);
        String systemPromptFinal = systemPrompt + "该用户ID为：" + userId;
        String uniqueConversationId = userId + "_" + conversationId;
        return chatClient.prompt()
                .tools(llmErrorRecordGet)
                .system(systemPromptFinal)
                .user(message)
                .advisors(advisorSpec -> advisorSpec.param(ChatMemory.CONVERSATION_ID, uniqueConversationId))
                .stream()
                .content();
    }
}
