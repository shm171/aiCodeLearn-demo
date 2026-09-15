package com.mylab.ailearn.core.service;

import com.mylab.ailearn.core.model.commonmodel.SourceFile;
import com.mylab.ailearn.core.model.commonmodel.StaticCheckReport;

/**
 * 规则静态检查：用固定规则对源码做硬性筛查（括号匹配、缩进、命名、使用未初始化变量、
 * 内存泄漏、空指针、数组越界、死循环）。
 *
 * <p>这是双层批改的第一层，只做可以确定性判定的问题；需要理解意图的逻辑与算法问题
 * 交给 LLM 深度批改。</p>
 */
public interface StaticCheckService {

    /**
     * 对源码做静态检查。
     *
     * @param sourceFile 已解析的源码；其 {@code language} 为 null 时按 C++ 处理
     * @return 规则违规列表汇总，永不返回 null
     * @throws org.springframework.web.server.ResponseStatusException sourceFile 为空时抛 400
     */
    StaticCheckReport check(SourceFile sourceFile);
}
