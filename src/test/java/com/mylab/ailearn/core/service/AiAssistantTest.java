package com.mylab.ailearn.core.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;


import java.util.UUID;

@SpringBootTest
public class AiAssistantTest {
    @Autowired
    private AiAssistant aiAssistant;
    @Test
    public void chatModelTest() {
        String content1 = "我叫什么名字";
        Long userId = 0001L;
        String conversationId = "0001";
        aiAssistant.aiChat(content1, userId,conversationId)
                .doOnNext(System.out::println)
                .blockLast();
        System.out.println();
    }
}
