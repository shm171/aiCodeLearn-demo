package com.mylab.ailearn.core.model.commonmodel;

/**
 * 一条编译错误。line 为从 1 起始的行号，0 表示无法定位。
 */
public record CompileError(
        int line,
        String message,
        String suggestion) {
}
