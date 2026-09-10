package com.mylab.ailearn.core.service;

import com.mylab.ailearn.core.model.commonmodel.ParseResult;

/**
 * 题目匹配：识别源码语言、匹配对应课程章节。
 *
 * <p>纯业务逻辑，不涉及数据库读写，供文件上传、AI 工具与批改工作流复用。</p>
 */
public interface ChapterMatchService {

    /**
     * 纯解析：先做输入预算与文件名校验（见 {@code SourceInputValidator}），
     * 再根据文件名后缀识别语言（.cpp → C++，.java → Java），最后按源码关键词匹配课程章节。
     *
     * @param filename 源码文件名，用于识别语言；只能包含字母、数字、下划线、连字符和点
     * @param content  源码内容，用于匹配章节；不超过 256 KB 与 5000 行
     * @return 解析结果（规范化后的文件名、语言、章节）
     * @throws org.springframework.web.server.ResponseStatusException 文件名/内容为空、文件名不合规、
     *         源码超出预算或语言不支持时抛 400
     */
    ParseResult parse(String filename, String content);
}
