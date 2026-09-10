package com.mylab.ailearn.core.enums;

/**
 * 批改错误的三个大类，与需求中的「格式错误 / 语法错误 / 逻辑错误」一一对应。
 */
public enum ErrorCategory {

    FORMAT_ERROR("格式错误"),
    SYNTAX_ERROR("语法错误"),
    LOGIC_ERROR("逻辑错误");

    private final String label;

    ErrorCategory(String label) {
        this.label = label;
    }

    public String label() {
        return label;
    }

    /**
     * 该分类在总分中的单次扣分权重，用于总分扣分与薄弱知识点权重计算。
     */
    public int deductionWeight() {
        return switch (this) {
            case FORMAT_ERROR -> 2;
            case SYNTAX_ERROR -> 10;
            case LOGIC_ERROR -> 8;
        };
    }
}