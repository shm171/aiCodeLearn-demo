package com.mylab.ailearn.core.model.commonmodel;

import com.mylab.ailearn.core.enums.ErrorCategory;
import com.mylab.ailearn.core.enums.RuleType;

/**
 * 规则静态检查发现的一处违规。
 *
 * <p>规则只在源码的代码部分匹配（注释与字符串已被剔除），因此 {@code line} 指向的是
 * 原始源码中的行号，可直接展示给学生。</p>
 *
 * @param ruleType   命中的规则，提供错误类型名与错误代码
 * @param category   该规则对应的错误大类；理论上取自 {@code ruleType.category()}，
 *                   但允许为 null，此时计分按兜底权重处理，避免无法归类的违规免罚
 * @param message    面向学生的违规说明
 * @param line       违规所在行号，从 1 开始；null 表示无法定位
 * @param suggestion 修改建议，可为 null
 */
public record RuleViolation(
        RuleType ruleType,
        ErrorCategory category,
        String message,
        Integer line,
        String suggestion) {
}
