package com.mylab.ailearn.core.dto;

/**
 * 双层批改中单个问题的来源。
 *
 *
 */
public enum IssueSource {
    /** 规则校验 Tool（Java 代码写的静态规则，功能文档第 2 点）。 */
    RULE_CHECK,
    /** LLM 深度批改（通义分析出的逻辑/思路问题，功能文档第 2 点）。 */
    LLM_DEEP_GRADING
}
