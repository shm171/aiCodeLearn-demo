package com.mylab.ailearn.core.model.commonmodel;

/**
 * 单个检查阶段的产出状态。
 */
public enum GradingStageOutcome {

    /** 完整完成，结论可直接采纳。 */
    COMPLETED,

    /** 只完成了一部分：结论可用但不完整（例如模型输出部分被校验丢弃）。 */
    PARTIAL,

    /** 按配置未启用（例如未接入 LLM 客户端），本次没有这一层的结论。 */
    NOT_CONFIGURED,

    /** 本该执行却没有产出可信结论（调用失败、输出整体不可采信）。 */
    UNAVAILABLE
}
