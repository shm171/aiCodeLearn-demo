package com.mylab.ailearn.core.service;

import com.mylab.ailearn.core.model.commonmodel.GradingResult;
import com.mylab.ailearn.core.model.commonmodel.SourceFile;

/**
 * 双层批改：先规则静态筛查，最后交给 LLM 深度批改，
 * 把双方结果合并、去重、分类并计分。
 */
public interface GradingService {

    /**
     * 对一份已解析的源码做完整批改。
     *
     * <p>规则静态检查与 LLM 深度批改的结果会合并去重，
     * 并据此计算总分（0~100）与生成反馈文案；LLM 未接入时自动降级为仅规则批改。</p>
     *
     * @param sourceFile 已解析的源码
     * @return 批改结果：问题清单 + 总分 + 反馈
     * @throws org.springframework.web.server.ResponseStatusException sourceFile 为空时抛 400
     */
    GradingResult grade(SourceFile sourceFile);
}
