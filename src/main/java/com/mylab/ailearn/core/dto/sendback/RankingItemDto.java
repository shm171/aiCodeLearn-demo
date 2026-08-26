package com.mylab.ailearn.core.dto.sendback;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 易错知识点排行单条记录。
 *///
@Getter
@AllArgsConstructor
public class RankingItemDto {

    /** 排名序号，从 1 开始。 */
    private final Integer rank;

    /** 知识点名称，例如 "数组越界"、"指针释放"。 */
    private final String topic;

    /** 该知识点下的错题总数。 */
    private final Long errorCount;
}
