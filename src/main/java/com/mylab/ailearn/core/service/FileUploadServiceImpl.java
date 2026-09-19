package com.mylab.ailearn.core.service;

import com.mylab.ailearn.core.model.commonmodel.ParseResult;
import com.mylab.ailearn.core.model.commonmodel.SourceFile;
import com.mylab.ailearn.core.service.spi.SourceFileStore;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 文件上传的默认实现。语言识别与章节匹配委托给 {@link ChapterMatchService}，
 * 归档通过 SourceFileStore 端口完成。
 */
@Service
@RequiredArgsConstructor
public class FileUploadServiceImpl implements FileUploadService {

    private final SourceFileStore sourceFileStore;
    private final ChapterMatchService chapterMatchService;

    @Override
    @Transactional
    public SourceFile receive(Long ownerUserId, String filename, String content) {
        ServiceSupport.requireOwner(ownerUserId);
        ParseResult parsed = chapterMatchService.parse(filename, content);

        SourceFile file = new SourceFile(
                null,
                ownerUserId,
                parsed.filename(),
                parsed.language(),
                parsed.chapter(),
                content,
                LocalDateTime.now());

        return sourceFileStore.save(file);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SourceFile> listByOwner(Long ownerUserId) {
        ServiceSupport.requireOwner(ownerUserId);
        return List.copyOf(ServiceSupport.nullToEmpty(sourceFileStore.findByOwnerUserId(ownerUserId)));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<SourceFile> listByOwner(Long ownerUserId, Pageable pageable) {
        ServiceSupport.requireOwner(ownerUserId);
        return sourceFileStore.findByOwnerUserId(ownerUserId, pageable);
    }
}
