package com.mylab.ailearn.core.service;

import com.mylab.ailearn.core.model.commonmodel.SourceFile;
import com.mylab.ailearn.core.model.commonmodel.StaticCheckReport;

/**
 * 规则静态检查：用 Java 固定规则对源码做硬性静态筛查（括号匹配、缩进、命名、
 * 内存泄漏、数组越界、空指针、死循环等）。
 */
public interface StaticCheckService {

    /**
     * 对源码做静态筛查。
     *
     * @param sourceFile 已解析的源码
     * @return 规则违规列表汇总，永不返回 null
     * @throws org.springframework.web.server.ResponseStatusException sourceFile 为空时抛 400
     */
    StaticCheckReport check(SourceFile sourceFile);
}
