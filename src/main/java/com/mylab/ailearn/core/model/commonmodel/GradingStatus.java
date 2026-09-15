package com.mylab.ailearn.core.model.commonmodel;

/**
 * 一次批改的整体完成度，用来判断分数能不能当正式成绩用。
 *
 * <p>「没有检测结果」不等于「检查通过」：只要有关键阶段没有产出可信结论，
 * 结果就不会是 {@link #COMPLETED}，{@link GradingResult#scoreAdoptable()} 也会是 false。</p>
 */
public enum GradingStatus {

    /** 所有已接入的检查阶段都完整完成，分数可采纳。 */
    COMPLETED,

    /** 有阶段只完成了一部分（模型输出部分被丢弃，或某阶段按配置未启用），分数仅供参考。 */
    PARTIAL,

    /** 有阶段本该执行却没产出可信结论（调用失败、输出不可采信），分数只覆盖了部分检查。 */
    UNAVAILABLE
}
