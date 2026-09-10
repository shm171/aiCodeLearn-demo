package com.mylab.ailearn.core.service.ai.tool;

import com.mylab.ailearn.core.enums.CourseChapter;
import com.mylab.ailearn.core.enums.ProgrammingLanguage;
import com.mylab.ailearn.core.model.commonmodel.SourceFile;
import com.mylab.ailearn.core.model.commonmodel.StaticCheckReport;
import com.mylab.ailearn.core.service.StaticCheckService;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * 静态代码检查 {@link Tool}。
 *
 * <p>把 Service 层已有的纯规则检查能力暴露为 Spring AI 可调用工具，
 * 供 ChatClient / Graph 智能体在批改流程中按需调用。</p>
 */
@Service
@RequiredArgsConstructor
public class CodeStaticCheckTool {

    private final StaticCheckService staticCheckService;

    /**
     * 对源码做硬性静态筛查，返回规则违规列表。
     */
    @Tool(name = "staticCodeCheck",
            description = "对 C++ / Java 学生源码执行规则静态检查，返回括号匹配、缩进、命名、"
                    + "内存泄漏、数组越界、空指针等违规列表")
    public StaticCheckReport check(
            @ToolParam(required = true, description = "源码文件名，例如 main.cpp 或 Main.java") String filename,
            @ToolParam(required = true, description = "源码的完整文本内容") String content) {

        ProgrammingLanguage language = ProgrammingLanguage.detect(filename)
                .orElse(ProgrammingLanguage.CPP);
        CourseChapter chapter = CourseChapter.match(filename, content);

        SourceFile sourceFile = new SourceFile(
                null, null, filename, language, chapter, content, LocalDateTime.now());

        return staticCheckService.check(sourceFile);
    }
}
