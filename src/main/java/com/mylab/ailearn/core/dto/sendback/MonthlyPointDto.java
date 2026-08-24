package com.mylab.ailearn.core.dto.sendback;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 月度学习曲线单个数据点。
 */
@Getter
@AllArgsConstructor
public class MonthlyPointDto {

    /** 月份标签，格式 "yyyy-MM"，例如 "2026-08"。 */
    private final String month;

    /** 该月新增错题数。 */
    private final Long errorCount;

    /** 该月提交总数。 */
    private final Long submissionCount;
}
