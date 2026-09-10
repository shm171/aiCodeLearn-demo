package com.mylab.ailearn.core.model.commonmodel;

import java.util.Objects;

/**
 * 单个检查阶段的状态报告：阶段、产出状态与一句可读说明。
 *
 * <p>机器可读的状态是 {@code outcome}，{@code detail} 只用于日志与展示，不应被当作判断依据。</p>
 */
public record GradingStageReport(
        GradingStage stage,
        GradingStageOutcome outcome,
        String detail) {

    public GradingStageReport {
        Objects.requireNonNull(stage, "stage 不能为空");
        // 状态缺失时按「不可采信」处理，避免出现默认可信的漏洞
        outcome = outcome == null ? GradingStageOutcome.UNAVAILABLE : outcome;
        detail = detail == null ? "" : detail;
    }
}
