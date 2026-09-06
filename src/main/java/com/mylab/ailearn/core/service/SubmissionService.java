package com.mylab.ailearn.core.service;

import com.mylab.ailearn.core.dto.SubmissionLanguage;
import com.mylab.ailearn.core.dto.sendback.SubmissionResponse;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

/**
 * 文件上传与解析的服务契约。
 *
 *
 * <ul>
 *   <li>文件名/语言检测：临时实现，可被保留或重写；</li>
 *   <li>读取文件内容、匹配章节、写入 core 表。当前返回 null 占位。</li>
 * </ul>
 *
 * <p>表结构（含 {@code owner_user_id} 归属字段）与 Flyway 迁移版本
 * 需要先与项目负责人协调（README 第五节、第七节）。
 */
@Service
public class SubmissionService {

    /**
     * 接收源码文件并解析基本信息。
     *
     * @param file        客户端上传的 .cpp / .java 源码
     * @param ownerUserId 当前登录账号 ID，由 Controller 从 SecurityContext 解析后传入；
     *                    core 只持有稳定 Long ID，不依赖 AppUser / Profile（README 第七节）
     */
    public SubmissionResponse upload(MultipartFile file, Long ownerUserId) {
        if (file == null || file.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "file is empty");
        }

        String fileName = file.getOriginalFilename();
        SubmissionLanguage language = detectLanguage(fileName);

        // TODO 读取代码内容、匹配课程章节（功能文档第 1 点）。
        String chapter = null;

        // TODO 将上传记录写入 core 自己的表，owner_user_id 作为账号归属。
        //   表结构需先与负责人确认并新增 Flyway V4 迁移（README 第五节）。
        Long submissionId = null;

        return new SubmissionResponse(submissionId, fileName, language, chapter);
    }

    private SubmissionLanguage detectLanguage(String fileName) {
        if (fileName == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "file name is missing");
        }
        String lower = fileName.toLowerCase();
        if (lower.endsWith(".java")) {
            return SubmissionLanguage.JAVA;
        }
        if (lower.endsWith(".cpp") || lower.endsWith(".cc") || lower.endsWith(".cxx")) {
            return SubmissionLanguage.CPP;
        }
        throw new ResponseStatusException(HttpStatus.UNSUPPORTED_MEDIA_TYPE,
                "only .java / .cpp source files are supported");
    }
}
