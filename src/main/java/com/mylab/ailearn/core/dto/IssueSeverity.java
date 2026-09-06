package com.mylab.ailearn.core.dto;

/**
 * 错误严重程度。
 *
 *
 */
public enum IssueSeverity {
    /** 格式/命名规范等警告类问题。 */
    INFO,
    /** 不致命但可能扣分的问题。 */
    WARNING,
    /** 编译不通过、运行时崩溃、关键逻辑错误。 */
    ERROR
}
