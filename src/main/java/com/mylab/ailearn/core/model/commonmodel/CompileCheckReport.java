package com.mylab.ailearn.core.model.commonmodel;

import java.util.List;

/**
 * 一次编译检查的结果。
 */
public record CompileCheckReport(
        CompileStatus status,
        List<CompileError> errors) {

    public CompileCheckReport {
        errors = errors == null ? List.of() : List.copyOf(errors);
    }
}
