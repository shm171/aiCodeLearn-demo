package com.mylab.ailearn.core.repository;

import com.mylab.ailearn.core.model.commonmodel.SourceFile;
import com.mylab.ailearn.core.service.spi.SourceFileStore;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * {@link SourceFileStore} 的 JPA 落地实现：负责 core 模型记录与实体之间的转换，
 * 让 Service 层的持久化端口真正落库。
 */
@Repository
@RequiredArgsConstructor
public class SourceFileStoreImpl implements SourceFileStore {

    private final SourceFileJpaRepository jpaRepository;

    @Override
    @Transactional
    public SourceFile save(SourceFile file) {
        SourceFileEntity entity = file.id() == null
                ? new SourceFileEntity()
                : jpaRepository.findById(file.id()).orElseGet(SourceFileEntity::new);

        entity.setOwnerUserId(file.ownerUserId());
        entity.setFilename(file.filename());
        entity.setLanguage(file.language());
        entity.setChapter(file.chapter());
        entity.setContent(file.content());
        entity.setSubmittedAt(file.submittedAt());

        SourceFileEntity saved = jpaRepository.save(entity);
        return toRecord(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<SourceFile> findById(Long id) {
        return jpaRepository.findById(id).map(this::toRecord);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SourceFile> findByOwnerUserId(Long ownerUserId) {
        return jpaRepository.findByOwnerUserId(ownerUserId).stream().map(this::toRecord).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<SourceFile> findAll() {
        return jpaRepository.findAll().stream().map(this::toRecord).toList();
    }

    private SourceFile toRecord(SourceFileEntity entity) {
        return new SourceFile(
                entity.getId(),
                entity.getOwnerUserId(),
                entity.getFilename(),
                entity.getLanguage(),
                entity.getChapter(),
                entity.getContent(),
                entity.getSubmittedAt());
    }
}
