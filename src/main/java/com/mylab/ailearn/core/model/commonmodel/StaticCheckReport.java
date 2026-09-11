package com.mylab.ailearn.core.model.commonmodel;

import java.util.List;

/**
 * 规则静态检查的输出：一次筛查发现的全部违规。
 *
 * @param violations 违规列表；无违规时为空列表，不为 null
 */
public record StaticCheckReport(List<RuleViolation> violations) {

    public StaticCheckReport {
        violations = violations == null ? List.of() : List.copyOf(violations);
    }

    /** 违规条数。 */
    public int errorCount() {
        return violations.size();
    }
}
