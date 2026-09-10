package com.mylab.ailearn.core.enums;

/**
 * 规则校验 Tool 中的固定静态规则。
 * 每条规则自带错误分类，先做硬性静态筛查，筛不出的逻辑错误再交给 LLM 深度批改。
 */
public enum RuleType {

    BRACE_MISMATCH("BRACE_MISMATCH", "花括号不匹配", ErrorCategory.SYNTAX_ERROR),
    PAREN_MISMATCH("PAREN_MISMATCH", "圆括号不匹配", ErrorCategory.SYNTAX_ERROR),
    INDENTATION("INDENTATION", "缩进不一致", ErrorCategory.FORMAT_ERROR),
    NAMING_CONVENTION("NAMING_CONVENTION", "变量命名不规范", ErrorCategory.FORMAT_ERROR),
    MEMORY_LEAK("MEMORY_LEAK", "疑似内存泄漏", ErrorCategory.LOGIC_ERROR),
    ARRAY_OUT_OF_BOUNDS("ARRAY_OUT_OF_BOUNDS", "数组越界访问", ErrorCategory.LOGIC_ERROR),
    NULL_POINTER_DEREFERENCE("NULL_POINTER_DEREFERENCE", "空指针/野指针解引用", ErrorCategory.LOGIC_ERROR),
    UNINITIALIZED_VARIABLE("UNINITIALIZED_VARIABLE", "使用未初始化变量", ErrorCategory.LOGIC_ERROR),
    INFINITE_LOOP("INFINITE_LOOP", "疑似死循环", ErrorCategory.LOGIC_ERROR);

    private final String code;
    private final String name;
    private final ErrorCategory category;

    RuleType(String code, String name, ErrorCategory category) {
        this.code = code;
        this.name = name;
        this.category = category;
    }

    public String code() {
        return code;
    }

    public String label() {
        return name;
    }

    public ErrorCategory category() {
        return category;
    }
}