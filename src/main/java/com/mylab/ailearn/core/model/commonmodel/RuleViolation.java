package com.mylab.ailearn.core.model.commonmodel;

import com.mylab.ailearn.core.enums.ErrorCategory;
import com.mylab.ailearn.core.enums.RuleType;

/**
 * 规则校验发现的一处违规。
 */
public record RuleViolation(
        RuleType ruleType,
        ErrorCategory category,
        String message,
        Integer line,
        String suggestion) {
}