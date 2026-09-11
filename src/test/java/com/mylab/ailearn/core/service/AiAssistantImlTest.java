package com.mylab.ailearn.core.service;

import com.mylab.ailearn.core.service.ai.AiAssistantIml;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * AI 学习助手的集成测试，直接注入真实实现 {@link AiAssistantIml} 观察其行为。
 *
 * <p><b>运行前提与风险</b>：这是 {@code @SpringBootTest}，会启动完整 Spring 上下文，
 * 需要可用的数据库（Flyway 迁移 + chat memory 表）与有效的 {@code DEEPSEEK_APIKEY}。
 * 运行时会真实调用模型、把回复打印到控制台并产生费用；{@code blockLast()} 未设置超时，
 * 模型无响应时会一直等待。因此本类不适合放进日常 / CI 回归，建议本地按需执行。</p>
 *
 * <p>使用的测试数据：用户 ID 固定为 1，会话 ID 为 "0001"。没有断言，
 * 只是把回复打印出来供人工观察。</p>
 */
@SpringBootTest
public class AiAssistantImlTest {
    @Autowired
    private AiAssistantIml aiAssistantIml;
    @Test
    public void chatModelTest() {
        String content1 = "分析我的薄弱点";
        Long userId = 0001L;
        String conversationId = "0001";
        aiAssistantIml.aiChat(content1, userId,conversationId)
                .doOnNext(System.out::println)
                .blockLast();
        System.out.println();
    }
}
