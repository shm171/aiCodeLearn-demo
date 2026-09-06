package com.mylab.ailearn.core.dto.sendback;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

/**
 * 双层批改结果。功能文档第 2 点：规则校验 Tool + LLM 深度批改。
 *
 *
 */
@Getter
@AllArgsConstructor
public class GradingResponse {

    /** 本次批改进程属于哪个 submission。 */
    private final Long submissionId;

    /** 本次批改总分。 */
    private final Integer score;

    /** 规则/LLM 查出的所有问题，按严重程度与行号排序。 */
    private final List<IssueDto> issues;

    /** LLM 给出的整体反馈；仅 LLM 深度批改生效时非 null。 */
    private final String overallFeedback;
}
