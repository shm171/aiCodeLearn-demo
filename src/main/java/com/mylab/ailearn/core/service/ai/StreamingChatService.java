package com.mylab.ailearn.core.service.ai;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

/**
 * 流式对话 Service。
 *
 * <p>基于 Spring AI {@link ChatClient} 提供同步与流式两种调用方式。
 * 流式方法返回 {@link Flux}{@code <String>}，供 Controller 负责人以
 * {@code text/event-stream} 输出时逐字返回模型生成内容。</p>
 */
@Service
public class StreamingChatService {

    private final ObjectProvider<ChatModel> chatModelProvider;

    public StreamingChatService(@Qualifier("DeepSeek") ObjectProvider<ChatModel> chatModelProvider) {
        this.chatModelProvider = chatModelProvider;
    }

    /**
     * 流式对话：逐 token 返回模型生成内容。
     *
     * @param message 用户输入的消息
     * @return 流式回答内容
     * @throws IllegalStateException 未配置 ChatModel 时抛出
     */
    public Flux<String> stream(String message) {
        return client().prompt().user(message).stream().content();
    }

    /**
     * 同步对话：一次性返回完整回答。
     *
     * @param message 用户输入的消息
     * @return 完整回答内容
     * @throws IllegalStateException 未配置 ChatModel 时抛出
     */
    public String chat(String message) {
        return client().prompt().user(message).call().content();
    }

    private ChatClient client() {
        ChatModel model = chatModelProvider.getIfAvailable();
        if (model == null) {
            throw new IllegalStateException("尚未配置 Spring AI ChatModel，无法进行对话");
        }
        return ChatClient.builder(model).build();
    }
}
