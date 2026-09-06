package com.mylab.ailearn.core.dto.sendback;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 学生薄弱知识点单条记录。功能文档第 3 点：薄弱知识点报告。
 */
@Getter
@AllArgsConstructor
public class WeakTopicDto {

    /** 知识点名称，例如 "数组越界"、"命名规范"。 */
    private final String topic;

    /** 该知识点下累计错题数。 */
    private final Long errorCount;

    /** 该知识点累计扣分权重，队友实现时定义计分规则。 */
    private final Double weight;

    /** 最近一次出现该知识点错误的时间（ISO 字符串）。 */
    private final String lastSeenAt;
}
