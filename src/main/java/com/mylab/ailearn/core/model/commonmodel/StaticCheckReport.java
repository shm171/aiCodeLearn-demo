package com.mylab.ailearn.core.model.commonmodel;

import java.util.List;

/**
 * 规则校验 Tool 的输出，汇总全部静态违规。
 */
    public record StaticCheckReport(List<RuleViolation> violations) {

    public StaticCheckReport {
        violations = violations == null ? List.of() : List.copyOf(violations);
    }

    public boolean hasErrors() {
        return !violations.isEmpty();
    }

    public int errorCount() {
        return violations.size();
    }
}