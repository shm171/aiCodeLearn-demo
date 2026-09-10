package com.mylab.ailearn.core.model.specialmodel;

import java.util.List;

/**
 * 错题分布数据，可直接用于饼图展示。
 */
public record ErrorDistribution(
        List<PieSlice> slices,
        long total) {

    public ErrorDistribution {
        slices = slices == null ? List.of() : List.copyOf(slices);
    }
}