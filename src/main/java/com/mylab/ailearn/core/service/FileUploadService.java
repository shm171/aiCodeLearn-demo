package com.mylab.ailearn.core.service;

import com.mylab.ailearn.core.model.commonmodel.SourceFile;

import java.util.List;

/**
 * 文件上传：接收 .cpp / .java 源码，识别语言、匹配章节后归档。
 */
public interface FileUploadService {

    /**
     * 接收并归档一份源码：先校验输入预算与文件名，再解析（识别语言、匹配章节），最后落库。
     *
     * @param ownerUserId 提交者用户 ID
     * @param filename    源码文件名（.cpp / .java），只能包含字母、数字、下划线、连字符和点
     * @param content     源码内容，不超过 256 KB 与 5000 行
     * @return 落库后的 SourceFile（id 已回填）
     * @throws org.springframework.web.server.ResponseStatusException ownerUserId 无效、文件名/内容为空或不合法、
     *         源码超出预算、语言不支持时抛 400（基础设施故障不在此列，会以服务端错误抛出）
     */
    SourceFile receive(Long ownerUserId, String filename, String content);

    /**
     * 查询某个学生名下的全部源码提交。
     *
     * @param ownerUserId 学生用户 ID
     * @return 该学生的源码列表（无提交时返回空列表，不会为 null）
     * @throws org.springframework.web.server.ResponseStatusException ownerUserId 无效时抛 400
     */
    List<SourceFile> listByOwner(Long ownerUserId);
}
