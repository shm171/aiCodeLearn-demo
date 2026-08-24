package com.mylab.ailearn.core.dto.sendback;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 教师看板中单个学生的错题统计，包括定位后进生。
 *
 *
 * 不返回学生姓名/邮箱；前端如需显示姓名，另调 base 模块用户查询接口。
 */
@Getter
@AllArgsConstructor
public class StudentErrorStatDto {

    /** 学生账号 ID。 */
    private final Long ownerUserId;

    /** 该学生累计错题数。 */
    private final Long errorCount;

    /** 该学生累计提交数。 */
    private final Long submissionCount;

    /** 最近一次提交时间（ISO 字符串），用于判断活跃度。 */
    private final String lastActiveAt;

    /** 是否标记为待关注（错题频率显著上升）。 */
    private final Boolean attention;
}
