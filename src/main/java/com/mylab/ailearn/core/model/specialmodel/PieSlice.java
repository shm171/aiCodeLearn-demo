package com.mylab.ailearn.core.model.specialmodel;

/**
 * 饼图中的一个扇区：标签与数量。
 */
public record PieSlice(
        String label,
        long value) {
}