package com.mylab.ailearn.core.enums;

/** 错题严重程度，用于归档展示与数据库过滤。 */
public enum ErrorSeverity {
    ERROR,
    WARNING,
    INFO;

    public static ErrorSeverity fromCategory(ErrorCategory category) {
        if (category == null) {
            return INFO;
        }
        return switch (category) {
            case SYNTAX_ERROR -> ERROR;
            case LOGIC_ERROR -> WARNING;
            case FORMAT_ERROR -> INFO;
        };
    }
}
