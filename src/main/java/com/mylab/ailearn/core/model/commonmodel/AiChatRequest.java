package com.mylab.ailearn.core.model.commonmodel;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AiChatRequest(
        @Schema(description = "用户发送给 AI 助手的消息")
        @NotBlank(message = "消息不能为空,如果是空的消息返回400")
        @Size(max = 2000)
        String message,

        @Schema(description = "会话ID：首次对话不传，服务端生成后通过响应头 X-Conversation-Id返回；之后原样传回以保持多轮记忆。最长 32 字符")
        @Size(max = 32, message = "会话ID长度不能超过32")
        String conversationId
) {

}
