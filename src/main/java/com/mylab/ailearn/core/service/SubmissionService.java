package com.mylab.ailearn.core.service;

import com.mylab.ailearn.core.dto.SubmissionLanguage;
import com.mylab.ailearn.core.dto.sendback.SubmissionResponse;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.util.concurrent.atomic.AtomicLong;

/**
 * 文件上传与解析的服务契约。
 *
 * <p>临时实现：用内存自增ID生成submissionId，等后端建表后替换。
 */
@Service
public class SubmissionService {

    // 临时：内存自增ID，等后端建表后替换
    private static final AtomicLong idGenerator = new AtomicLong(1000);

    public SubmissionResponse upload(MultipartFile file, Long ownerUserId) {
        if (file == null || file.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "file is empty");
        }

        String fileName = file.getOriginalFilename();
        SubmissionLanguage language = detectLanguage(fileName);

        String chapter = null;

        // 临时：用内存自增ID
        Long submissionId = idGenerator.incrementAndGet();

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
