package com.mylab.ailearn.core.service.ai;


import com.mylab.ailearn.core.service.ai.tool.LlmErrorRecordGet;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

/**
 * {@link AiAssistant} 的 Spring AI 实现。
 *
 * <p>用容器里的 {@code ChatClient}（已挂会话记忆增强器）调用 DeepSeek，并向模型注册
 * {@link LlmErrorRecordGet} 工具，使助手可以按用户 ID 查询错题。记忆键是「用户 ID + 会话 ID」，
 * 因此同一用户的不同会话、以及不同用户的会话互不串扰。</p>
 *
 * <p>系统提示词在构造时拼好，调用时再把当前用户 ID 追加进去；关于身份的注意事项见
 * {@link AiAssistant} 的接口注释。</p>
 */
@Service
public class AiAssistantIml implements AiAssistant {

    private final ChatClient chatClient;
    private final LlmErrorRecordGet llmErrorRecordGet;
    private final String systemPrompt;

    public AiAssistantIml(ChatClient chatClient, LlmErrorRecordGet llmErrorRecordGet) {
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
     * {@inheritDoc}
     *
     * <p>实现细节：把当前用户 ID 追加进系统提示词后发起流式请求，
     * 并用「用户 ID_会话 ID」作为 {@link ChatMemory#CONVERSATION_ID} 的参数值。</p>
     */
    @Override
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
