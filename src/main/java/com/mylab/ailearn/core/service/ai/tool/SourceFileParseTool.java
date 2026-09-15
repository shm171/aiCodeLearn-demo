package com.mylab.ailearn.core.service.ai.tool;

import com.mylab.ailearn.core.model.commonmodel.ParseResult;
import com.mylab.ailearn.core.model.commonmodel.SourceFile;
import org.springframework.ai.tool.annotation.Tool;

import java.util.Objects;

/**
 * 源码解析 {@link Tool}。
 *
 * <p>把本次提交的解析结果（文件名、编程语言、课程章节）暴露给模型，
 * 便于它据此选择批改策略。</p>
 *
 * <p>与静态检查工具一致，源码在构造时绑定，模型不能传入其他文件来自定义批改对象。
 * 每次批改创建一个实例。</p>
 */
public class SourceFileParseTool {

    private final ParseResult parseResult;

    /**
     * @param sourceFile 本次提交的权威源码，语言与章节以其解析结果为准
     */
    public SourceFileParseTool(SourceFile sourceFile) {
        Objects.requireNonNull(sourceFile, "sourceFile 不能为空");
        this.parseResult = new ParseResult(sourceFile.filename(), sourceFile.language(), sourceFile.chapter());
    }

    @Tool(name = "parseSourceFile",
            description = "返回本次批改源码的解析信息：文件名、识别出的编程语言与匹配到的课程章节。无需传入参数。")
    public ParseResult parse() {
        return parseResult;
    }
}
