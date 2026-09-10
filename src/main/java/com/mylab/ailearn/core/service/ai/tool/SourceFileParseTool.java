package com.mylab.ailearn.core.service.ai.tool;

import com.mylab.ailearn.core.model.commonmodel.ParseResult;
import com.mylab.ailearn.core.service.ChapterMatchService;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Service;

/**
 * 源码解析 {@link Tool}。
 *
 * <p>把文件解析能力（识别语言、匹配课程章节、统计行数）暴露给智能体，
 * 支持智能体先解析、再决定后续批改策略。</p>
 */
@Service
@RequiredArgsConstructor
public class SourceFileParseTool {

    private final ChapterMatchService chapterMatchService;

    @Tool(name = "parseSourceFile",
            description = "解析源码文件名与内容，识别编程语言、匹配课程章节并统计代码行数")
    public ParseResult parse(
            @ToolParam(required = true, description = "源码文件名，例如 main.cpp 或 Main.java") String filename,
            @ToolParam(required = true, description = "源码的完整文本内容") String content) {

        return chapterMatchService.parse(filename, content);
    }
}
