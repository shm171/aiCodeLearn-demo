package com.mylab.ailearn.core.service;

import com.mylab.ailearn.core.model.commonmodel.ParseResult;

/**
 * 题目匹配：识别源码语言、匹配对应课程章节。
 *
 * <p>纯业务逻辑，不涉及数据库读写，供文件上传、AI 工具与批改工作流复用。</p>
 */
public interface ChapterMatchService {

    /**
     * 纯解析：根据文件名后缀识别语言（.cpp → C++，.java → Java），
     * 再按源码关键词匹配课程章节。
     *
     * @param filename 源码文件名，用于识别语言
     * @param content  源码内容，用于匹配章节
     * @return 解析结果（文件名、语言、章节）
     * @throws org.springframework.web.server.ResponseStatusException 文件名或内容为空、语言不支持时抛 400
     */
    ParseResult parse(String filename, String content);
}
