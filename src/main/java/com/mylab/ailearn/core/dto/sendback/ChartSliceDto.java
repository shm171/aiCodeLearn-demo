package com.mylab.ailearn.core.dto.sendback;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 饼图/柱状图的单个切片。{label, value} 结构，前端直接渲染。
 *///
@Getter
@AllArgsConstructor
public class ChartSliceDto {

    /** 分类名称，例如 "命名规范"、"WARNING"。 */
    private final String label;

    /** 该分类下的数量。 */
    private final Long value;
}
