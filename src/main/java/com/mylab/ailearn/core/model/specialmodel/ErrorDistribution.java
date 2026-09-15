package com.mylab.ailearn.core.model.specialmodel;

import java.util.List;

/**
 * 错题分布数据，可直接用于饼图展示。
 *
 * <p><b>注意 {@code total} 与 {@code slices} 的口径不同</b>：{@code total} 是参与统计的
 * 错题记录总条数（包含分类为 null 的记录），而 {@code slices} 只统计有分类的记录。
 * 因此 {@code slices} 各扇区之和可能小于 {@code total}；做百分比时请以 slices 之和为分母，
 * 否则扇区占比会不足 100%。</p>
 *
 * @param slices 按错误分类聚合的扇区，只包含数量大于 0 的分类
 * @param total  参与统计的错题记录总数
 */
public record ErrorDistribution(
        List<PieSlice> slices,
        long total) {

    public ErrorDistribution {
        slices = slices == null ? List.of() : List.copyOf(slices);
    }
}
