package com.mylab.ailearn.core.model.commonmodel;

import java.util.List;

/**
 * 批改后的最终结果：规则校验 + LLM 深度批改合并、去重、计分。
 *
 * <p>除了问题清单、分数与反馈，还带<b>结构化的各阶段状态</b>：调用方不必从自然语言里猜
 * 这个分数是否可信，直接读 {@link #status()} / {@link #stages()} /
 * {@link #scoreAdoptable()} 即可。没有检测结果不等于检查通过——只要有没有产出可信结论的阶段，
 * 分数就不能作为正式成绩。</p>
 */
public record GradingResult(
        List<GradedError> errors,
        int score,
        String feedback,
        GradingStatus status,
        List<GradingStageReport> stages) {

    public GradingResult {
        errors = errors == null ? List.of() : List.copyOf(errors);
        // 状态缺失时按「不可采纳」处理，避免出现默认可信的漏洞
        status = status == null ? GradingStatus.UNAVAILABLE : status;
        stages = stages == null ? List.of() : List.copyOf(stages);
    }

    /**
     * 分数能否作为正式成绩采纳。
     *
     * <p>只有整体状态为 {@link GradingStatus#COMPLETED} 且每个阶段都完整完成时才为 true；
     * 为 false 时该分数只覆盖了部分检查，仅可作为学习参考。</p>
     */
    public boolean scoreAdoptable() {
        return status == GradingStatus.COMPLETED
                && stages.stream().allMatch(stage -> stage.outcome() == GradingStageOutcome.COMPLETED);
    }

    /** 查询某一阶段的状态报告；未记录该阶段时返回 null。 */
    public GradingStageReport stage(GradingStage stage) {
        return stages.stream()
                .filter(report -> report.stage() == stage)
                .findFirst()
                .orElse(null);
    }
}
