package com.mylab.ailearn.core.service.ai;

import reactor.core.publisher.Flux;

/**
 * AI 学习助手的服务契约。
 *
 * <p>面向学生提供与批改系统相关的问答：总结错题与薄弱点、讲解易错知识点、推荐训练章节。
 * 当前由 {@link AiAssistantIml} 基于 Spring AI 的 ChatClient 实现。</p>
 *
 * <p><b>安全边界（重要）</b>：{@code ownerUserId} 只是写进提示词、交给模型的上下文，
 * <b>不构成任何数据权限</b>——模型查库时用的是它自己生成的工具调用参数（见
 * {@code LlmErrorRecordGet}）。要做数据隔离，必须在工具侧改用服务端可信身份而不是模型给出的 ID
 * （当前尚未实现）。调用方仍应从已认证身份取得该 ID，不要直接用请求参数里的用户 ID。</p>
 */
public interface AiAssistant {

    /**
     * 流式 AI 对话：逐段返回助手回答，供 Controller 以 {@code text/event-stream} 输出。
     *
     * @param message        用户输入的消息
     * @param ownerUserId    当前登录用户 ID，仅作为提示词上下文，不构成数据权限（见接口注释）
     * @param conversationId 会话 ID，与用户 ID 一起构成记忆键，用于隔离不同会话
     * @return 流式回答内容
     */
    Flux<String> aiChat(String message, Long ownerUserId, String conversationId);
}
