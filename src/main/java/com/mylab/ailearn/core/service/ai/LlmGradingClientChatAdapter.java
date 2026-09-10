package com.mylab.ailearn.core.service.ai;

import com.mylab.ailearn.core.model.commonmodel.LlmReview;
import com.mylab.ailearn.core.model.commonmodel.SourceFile;
import com.mylab.ailearn.core.service.ai.tool.CodeLineLocatorTool;
import com.mylab.ailearn.core.service.ai.tool.CodeStaticCheckTool;
import com.mylab.ailearn.core.service.ai.tool.SourceFileParseTool;
import com.mylab.ailearn.core.service.spi.LlmGradingClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * {@link LlmGradingClient} 的 Spring AI 实现。
 *
 * <p>通过 {@link ChatClient} 调用已配置的通义 / 任意兼容 ChatModel，对规则静态检查
 * 筛不出的逻辑错误与算法思路做深度批改，并使用结构化输出把结果映射为
 * {@link LlmReview}。本适配器同时把 {@link CodeStaticCheckTool} 与
 * {@link SourceFileParseTool} 注册为模型可调用的工具，方便模型复核静态结论。</p>
 *
 * <p>ChatModel 通过 {@link ObjectProvider} 注入：未配置模型时优雅降级为
 * 空的 {@link LlmReview}，不阻断规则校验路径。</p>
 */
@Slf4j
@Service
public class LlmGradingClientChatAdapter implements LlmGradingClient {

    private static final String SYSTEM_PROMPT = """
            你是大一程序设计课程的助教，负责对学生的 C++ / Java 源码做深度批改。
            重点补充规则静态检查筛不出的逻辑错误、算法思路、边界条件、异常处理与代码坏味道。

            必须严格只输出一个 JSON 对象，不要输出任何解释文字、Markdown 代码块或多余内容。

            输出 JSON 结构如下：
            {
              "issues": [
                {
                  "category": "LOGIC_ERROR",
                  "errorType": "数组越界访问",
                  "errorCode": "ARRAY_OUT_OF_BOUNDS",
                  "fixSuggestion": "把循环条件从 <= n 改为 < n，确保下标在 0..n-1 内",
                  "source": "LLM 深度批改",
                  "line": [12]
                }
              ],
              "summary": "整体批改结论"
            }

            字段约束（必须严格遵守）：
            1. category 只能是 ErrorCategory 枚举之一，取值必须是下面三者之一：
               - FORMAT_ERROR（格式错误）
               - SYNTAX_ERROR（语法错误）
               - LOGIC_ERROR（逻辑错误）
            2. source 固定为 "LLM 深度批改"，不允许输出其他值。
            3. line 表示错误所在行号数组，从 1 开始计数。禁止自行猜测行号，每个行号都必须通过调用工具 locateCodeLine 获得；同一类错误出现在多处时，把工具返回的所有行号都放进数组；若工具返回空数组，line 就填空数组 []。
            4. errorType 与 errorCode 优先从 RuleType 枚举中选取；只有该问题不属于下表任何一项时，才按末尾规则自行生成。

            RuleType 枚举对照表（errorType = 中文名，errorCode = 枚举 code，category = 对应分类）：
            - ARRAY_OUT_OF_BOUNDS | 数组越界访问 | LOGIC_ERROR
            - NULL_POINTER_DEREFERENCE | 空指针/野指针解引用 | LOGIC_ERROR
            - UNINITIALIZED_VARIABLE | 使用未初始化变量 | LOGIC_ERROR
            - INFINITE_LOOP | 疑似死循环 | LOGIC_ERROR
            - MEMORY_LEAK | 疑似内存泄漏 | LOGIC_ERROR
            - BRACE_MISMATCH | 花括号不匹配 | SYNTAX_ERROR
            - PAREN_MISMATCH | 圆括号不匹配 | SYNTAX_ERROR
            - INDENTATION | 缩进不一致 | FORMAT_ERROR
            - NAMING_CONVENTION | 变量命名不规范 | FORMAT_ERROR

            自行生成规则（仅当问题无法匹配上表时使用）：
            - errorType：用简短中文描述错误，例如“算法复杂度过高”“递归缺少终止条件”。
            - errorCode：参照 RuleType 枚举的命名风格，使用大写英文单词 + 下划线，简短表意。
              示例：
              - ALGORITHM_INCORRECT（算法思路不正确）
              - NO_BASE_CASE（递归缺少终止条件）
              - HIGH_COMPLEXITY（时间复杂度过高）
              禁止使用 L001、S001 这类编号写法。

            其他要求：
            - 每条 issue 的 category 必须与 errorType、errorCode 表达的错误类型保持一致。
            - fixSuggestion 要具体、可操作，直接告诉学生怎么改。
            - 若源码没有需要补充的问题，issues 返回空数组 []。
            - summary 用 1-2 句话概括整体质量与主要问题。
            """;

    private final ObjectProvider<ChatModel> chatModelProvider;
    private final CodeLineLocatorTool codeLineLocatorTool;
    private final CodeStaticCheckTool codeStaticCheckTool;
    private final SourceFileParseTool sourceFileParseTool;

    LlmGradingClientChatAdapter(
            @Qualifier("DeepSeek")
            ObjectProvider<ChatModel> chatModelProvider,
            CodeLineLocatorTool codeLineLocatorTool,
            CodeStaticCheckTool codeStaticCheckTool,
            SourceFileParseTool sourceFileParseTool) {
        this.chatModelProvider = chatModelProvider;
        this.codeLineLocatorTool = codeLineLocatorTool;
        this.codeStaticCheckTool = codeStaticCheckTool;
        this.sourceFileParseTool = sourceFileParseTool;
    }

    @Override
    public LlmReview review(SourceFile file) {
        ChatModel model = chatModelProvider.getIfAvailable();
        if (model == null) {
            return new LlmReview(List.of(), "尚未配置 Spring AI ChatModel，本次未执行 LLM 深度批改。");
        }
        try {
            ChatClient client = ChatClient.builder(model)
                    .defaultSystem(SYSTEM_PROMPT)
                    .defaultTools(codeStaticCheckTool, sourceFileParseTool, codeLineLocatorTool)//模型复核与行号定位
                    .build();

            return client.prompt()
                    .user(buildUserPrompt(file))
                    .call()
                    .entity(LlmReview.class);
        } catch (Exception e) {
            log.warn("LLM 深度批改调用失败，降级为仅规则校验：{}", e.getMessage(), e);
            return new LlmReview(List.of(), "LLM 调用失败，本次仅规则校验。");
        }
    }

    private String buildUserPrompt(SourceFile file) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("请批改下面的学生源码。\n");
        prompt.append("文件名：").append(file.filename()).append('\n');
        prompt.append("语言：").append(file.language() == null ? "未知" : file.language().displayName()).append('\n');
        prompt.append("章节：").append(file.chapter() == null ? "未匹配" : file.chapter().displayValue()).append('\n');
        prompt.append("源码内容：\n```\n")
                .append(file.content())
                .append("\n```\n");
        return prompt.toString();
    }
}
