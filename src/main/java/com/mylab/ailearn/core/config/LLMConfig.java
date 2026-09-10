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

@Configuration
public class LLMConfig {
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

    @Bean
    public ChatMemory chatMemory(JdbcChatMemoryRepository repo){
        return MessageWindowChatMemory.builder()
                .chatMemoryRepository(repo)
                .maxMessages(30)
                .build();
    }

    @Bean
    public ChatClient chatClient(@Qualifier("DeepSeek")ChatModel chatModel, ChatMemory memory) {
        ChatClient client = ChatClient.builder(chatModel)
                // 设置默认的增强器，类似spring的aop，在访问大模型的前后自动帮我们执行会话记忆的逻辑
                .defaultAdvisors(MessageChatMemoryAdvisor.builder(memory).build())
                .build();

        return client;
    }

}


