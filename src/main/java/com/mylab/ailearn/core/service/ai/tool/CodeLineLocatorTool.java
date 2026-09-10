package com.mylab.ailearn.core.service.ai.tool;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * 行号定位 {@link Tool}。
 *
 * <p>供 LLM 在深度批改时调用：模型给出错误对应的原始代码片段，由本工具在源码中
 * 精确定位行号，避免模型自行“猜行号”导致偏差。</p>
 */
@Service
public class CodeLineLocatorTool {

    @Tool(name = "locateCodeLine",
            description = "根据错误代码片段在源码中定位所有出现位置的行号。返回从 1 开始计数的行号数组；找不到时返回空数组。")
    public List<Integer> locate(
            @ToolParam(required = true, description = "错误对应的原始代码片段，尽量原样复制，不要改写") String codeSnippet,
            @ToolParam(required = true, description = "完整源码内容，必须与批改的源码原文一致") String sourceContent) {

        if (sourceContent == null || codeSnippet == null || codeSnippet.isBlank()) {
            return List.of();
        }

        List<Integer> lines = new ArrayList<>();
        int from = 0;
        while (true) {
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
