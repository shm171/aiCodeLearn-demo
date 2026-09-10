package com.mylab.ailearn.core.model.commonmodel;

/**
 * 批改流程中的检查阶段。
 */
public enum GradingStage {

    /** 规则静态检查：括号匹配、缩进、命名、越界、空指针、死循环等固定规则。 */
    RULE_STATIC_CHECK,

    /** LLM 深度批改：规则筛不出的逻辑错误与算法思路问题。 */
    LLM_REVIEW
}
