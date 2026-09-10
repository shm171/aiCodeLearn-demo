package com.mylab.ailearn.core.model.specialmodel;

/**
 * 饼图中的一个扇区。
 *
 * @param label 扇区标签，用的是错误分类的中文名（格式错误 / 语法错误 / 逻辑错误）
 * @param value 该分类的错题条数
 */
public record PieSlice(
        String label,
        long value) {
}
