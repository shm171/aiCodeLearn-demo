package com.mylab.ailearn.core.service.ai.tool;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 行号定位 {@link Tool}。
 *
 * <p>供 LLM 在深度批改时调用：模型给出错误对应的原始代码片段，由本工具在
 * <b>服务端持有、本次正在批改的源码</b>中定位行号，避免模型自行「猜行号」。</p>
 *
 * <p>源码由构造时绑定，模型无法传入或替换，因此不能用另一份源码伪造行号。
 * 每次批改创建一个实例，并带有调用次数与返回条数预算。</p>
 */
public class CodeLineLocatorTool {

    /** 单次批改内允许的定位调用次数上限，超出后返回空数组。 */
    public static final int MAX_CALLS = 30;
    /** 单次定位返回的行号条数上限。 */
    public static final int MAX_RESULTS = 100;
    /** 允许的代码片段长度上限，超出即视为异常输入。 */
    public static final int MAX_SNIPPET_LENGTH = 2000;

    /** 本次批改的权威源码，只读。 */
    private final String sourceContent;
    /** 已发生的调用次数，用于约束单次批改的工具调用预算。 */
    private final AtomicInteger calls = new AtomicInteger();

    /**
     * @param sourceContent 本次提交的权威源码内容；null 视为空源码
     */
    public CodeLineLocatorTool(String sourceContent) {
        this.sourceContent = sourceContent == null ? "" : sourceContent;
    }

    @Tool(name = "locateCodeLine",
            description = "在本次批改的源码中定位给定代码片段出现的所有行号。源码由服务端提供，无需也不能传入。"
                    + "返回从 1 开始计数的行号数组；找不到时返回空数组。")
    public List<Integer> locate(
            @ToolParam(required = true, description = "错误对应的原始代码片段，尽量原样复制，不要改写") String codeSnippet) {

        if (calls.incrementAndGet() > MAX_CALLS) {
            return List.of();
        }
        if (codeSnippet == null || codeSnippet.isBlank()
                || codeSnippet.length() > MAX_SNIPPET_LENGTH
                || sourceContent.isEmpty()) {
            return List.of();
        }

        List<Integer> lines = new ArrayList<>();
        int from = 0;
        while (lines.size() < MAX_RESULTS) {
            int index = sourceContent.indexOf(codeSnippet, from);
            if (index < 0) {
                break;
            }
            lines.add(lineAt(sourceContent, index));
            from = index + codeSnippet.length();
        }
        return lines;
    }

    private int lineAt(String text, int index) {
        String prefix = text.substring(0, Math.min(index, text.length()));
        return 1 + (int) prefix.chars().filter(c -> c == '\n').count();
    }
}
