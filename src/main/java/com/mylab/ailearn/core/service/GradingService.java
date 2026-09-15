package com.mylab.ailearn.core.service;

import com.mylab.ailearn.core.dto.IssueSeverity;
import com.mylab.ailearn.core.dto.IssueSource;
import com.mylab.ailearn.core.dto.sendback.GradingResponse;
import com.mylab.ailearn.core.dto.sendback.IssueDto;
import com.mylab.ailearn.core.dto.sendto.GradingRequest;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

/**
 * 规则校验 Tool + LLM 深度批改。
 *
 * <p>临时实现：返回模拟的批改结果，等后端实现真正的规则校验和LLM对接后替换。
 */
@Service
public class GradingService {

    public GradingResponse grade(Long submissionId, Long ownerUserId, GradingRequest request) {

        // 临时：模拟的批改结果，对应test_error.cpp里的3个错误
        List<IssueDto> issues = Arrays.asList(
            new IssueDto(
                IssueSource.RULE_CHECK,
                IssueSeverity.ERROR,
                "数组越界",
                6,
                "数组下标越界：长度为5的数组最大下标为4，循环条件 i<=5 会访问 arr[5]，导致运行时异常",
                "将循环条件改为 i < 5，即 for (int i = 0; i < 5; i++)"
            ),
            new IssueDto(
                IssueSource.RULE_CHECK,
                IssueSeverity.ERROR,
                "变量未定义",
                10,
                "变量 sum 未声明就直接使用，会导致编译失败",
                "在使用前声明变量：int sum = 0;"
            ),
            new IssueDto(
                IssueSource.RULE_CHECK,
                IssueSeverity.WARNING,
                "缺少分号",
                13,
                "cout 输出语句末尾缺少分号，C++ 语句必须以分号结尾",
                "在 cout << \"Hello EduCode\" << endl 末尾添加分号 ;"
            )
        );

        String overallFeedback = "本次代码存在3个问题，其中2个严重错误会导致编译失败。建议先修复数组越界和变量未定义问题，再检查语法细节。整体逻辑思路清晰，代码结构规范，继续加油！";

        return new GradingResponse(submissionId, 65, issues, overallFeedback);
    }
}
