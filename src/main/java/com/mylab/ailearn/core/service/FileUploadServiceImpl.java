package com.mylab.ailearn.core.service;

import com.mylab.ailearn.core.model.commonmodel.ParseResult;
import com.mylab.ailearn.core.model.commonmodel.SourceFile;
import com.mylab.ailearn.core.service.spi.SourceFileStore;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 文件上传的默认实现。语言识别与章节匹配委托给 {@link ChapterMatchService}，
 * 归档通过 SourceFileStore 端口完成。
 */
@Service
@RequiredArgsConstructor
public class FileUploadServiceImpl implements FileUploadService {

    private final ObjectProvider<SourceFileStore> sourceFileStoreProvider;
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

        return store().save(file);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SourceFile> listByOwner(Long ownerUserId) {
        ServiceSupport.requireOwner(ownerUserId);
        return List.copyOf(ServiceSupport.nullToEmpty(store().findByOwnerUserId(ownerUserId)));
    }

    private SourceFileStore store() {
        return ServiceSupport.required(sourceFileStoreProvider, "SourceFileStore");
    }
}
