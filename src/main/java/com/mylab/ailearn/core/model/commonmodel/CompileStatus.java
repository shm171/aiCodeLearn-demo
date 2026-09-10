package com.mylab.ailearn.core.model.commonmodel;

/**
 * 编译检查状态。
 */
public enum CompileStatus {
    /** 编译通过。 */
    PASSED,
    /** 编译失败（有编译错误）。 */
    FAILED,
    /** 编译器不可用，本次跳过编译检查。 */
    SKIPPED
}
