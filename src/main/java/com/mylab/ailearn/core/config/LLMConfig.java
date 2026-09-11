package com.mylab.ailearn.core.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.memory.repository.jdbc.JdbcChatMemoryRepository;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Spring AI 相关 Bean 的装配。
 *
 * <p>当前只接入 DeepSeek（走 OpenAI 兼容协议）。Bean 名固定为 {@code "DeepSeek"}，
 * 批改适配器按这个名字注入（{@code @Qualifier("DeepSeek")}），改名会同时影响模型 Bean
 * 与下面 {@code chatClient} 的装配。</p>
 *
 * <p><b>运行前提</b>：{@code DEEPSEEK_APIKEY} 必须存在，否则容器启动时解析占位符就会失败；
 * 会话记忆使用 JDBC 存储，需要可用的数据库（表结构由 Spring AI 自行初始化）。</p>
 */
@Configuration
public class LLMConfig {

    /**
     * DeepSeek 对话模型。Base URL、模型名与 API Key 都在这里写死 / 注入，
     * 换模型或换服务商时只需改本方法。
     *
     * @param apiKey 从环境变量 {@code DEEPSEEK_APIKEY} 读取，不落库、不写进仓库
     */
    @Bean(name = "DeepSeek")
    public ChatModel deepSeekChatModel(@Value("${DEEPSEEK_APIKEY}") String apiKey) {
        return OpenAiChatModel.builder()
                .openAiApi(
                        OpenAiApi.builder()
                                .apiKey(apiKey)
                                .baseUrl("https://api.deepseek.com")
                                .build()
                )
                .defaultOptions(
                        OpenAiChatOptions.builder()
                                .model("deepseek-chat")
                                .build()
                )
                .build();
    }

    /**
     * 会话记忆：把历史消息按会话 ID 存到数据库，并只保留最近 30 条，
     * 避免上下文无限增长导致 token 费用失控。
     */
    @Bean
    public ChatMemory chatMemory(JdbcChatMemoryRepository repo){
        return MessageWindowChatMemory.builder()
                .chatMemoryRepository(repo)
                .maxMessages(30)
                .build();
    }

    /**
     * 供 AI 助手使用的 ChatClient，默认挂了会话记忆增强器
     * （类似 AOP，在调用模型前后自动读写该会话的历史消息）。
     *
     * <p>会话 ID 由调用方通过 {@code ChatMemory.CONVERSATION_ID} 参数传入，
     * 不传则所有对话会共用同一段记忆。</p>
     */
    @Bean
    public ChatClient chatClient(@Qualifier("DeepSeek")ChatModel chatModel, ChatMemory memory) {
        ChatClient client = ChatClient.builder(chatModel)
                // 设置默认的增强器，类似spring的aop，在访问大模型的前后自动帮我们执行会话记忆的逻辑
                .defaultAdvisors(MessageChatMemoryAdvisor.builder(memory).build())
                .build();

        return client;
    }

}
