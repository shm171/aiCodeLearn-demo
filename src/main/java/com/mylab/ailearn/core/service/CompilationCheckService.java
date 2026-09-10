package com.mylab.ailearn.core.service;

import com.mylab.ailearn.core.model.commonmodel.CompileCheckReport;
import com.mylab.ailearn.core.model.commonmodel.SourceFile;

/**
 * 编译检查：用真实编译器编译学生源码（Java 用 javac，C++ 用 g++/clang++ 做语法检查），
 * 返回编译是否通过与编译错误列表。
 *
 * <p>只编译、不运行；编译器不可用时自动降级为「跳过」，不阻断批改流程。</p>
 */
public interface CompilationCheckService {

    /**
     * 编译学生源码。
     *
     * @param sourceFile 已解析的源码（包含语言与内容）
     * @return 编译检查结果：PASSED（通过）/ FAILED（失败，含错误列表）/ SKIPPED（编译器不可用，跳过），永不返回 null
     */
    CompileCheckReport check(SourceFile sourceFile);
}
