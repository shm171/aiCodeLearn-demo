package com.mylab.ailearn.core.service.ai.tool;

import com.mylab.ailearn.core.model.commonmodel.SourceFile;
import com.mylab.ailearn.core.model.commonmodel.StaticCheckReport;
import com.mylab.ailearn.core.service.StaticCheckService;
import org.springframework.ai.tool.annotation.Tool;

import java.util.Objects;

/**
 * 静态代码检查 {@link Tool}。
 *
 * <p>把 Service 层已有的纯规则检查能力暴露为 Spring AI 可调用工具，
 * 供模型在批改流程中复核规则结论。</p>
 *
 * <p>源码在构造时绑定为<b>本次正在批改的权威提交</b>，模型不能传入或替换文件名与内容，
 * 因此无法用另一份「干净源码」换取一份干净的检查结果。每次批改创建一个实例。</p>
 */
public class CodeStaticCheckTool {

    private final StaticCheckService staticCheckService;
    private final SourceFile sourceFile;

    /**
     * @param staticCheckService 规则静态检查能力
     * @param sourceFile         本次提交的权威源码，只读
     */
    public CodeStaticCheckTool(StaticCheckService staticCheckService, SourceFile sourceFile) {
        this.staticCheckService = Objects.requireNonNull(staticCheckService, "staticCheckService 不能为空");
        this.sourceFile = Objects.requireNonNull(sourceFile, "sourceFile 不能为空");
    }

    /**
     * 对本次提交的源码做规则静态检查，返回违规列表。
     */
    @Tool(name = "staticCodeCheck",
            description = "对本次批改的学生源码执行规则静态检查，返回括号匹配、缩进、命名、"
                    + "内存泄漏、数组越界、空指针等违规列表。源码由服务端提供，无需也不能传入。")
    public StaticCheckReport check() {
        return staticCheckService.check(sourceFile);
    }
}
