package com.mylab.ailearn.core.repository;

import com.mylab.ailearn.core.model.commonmodel.ErrorRecord;
import com.mylab.ailearn.core.service.spi.ErrorRecordStore;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * {@link ErrorRecordStore} 的 JPA 落地实现：负责 core 模型记录与实体之间的转换，
 * 出错行号列表以逗号分隔字符串存储。
 */
@Repository
@RequiredArgsConstructor
public class ErrorRecordStoreImpl implements ErrorRecordStore {

    private final ErrorRecordJpaRepository jpaRepository;

    @Override
    @Transactional
    public ErrorRecord save(ErrorRecord record) {
        ErrorRecordEntity entity = record.id() == null
                ? new ErrorRecordEntity()
                : jpaRepository.findById(record.id()).orElseGet(ErrorRecordEntity::new);

        applyRecord(entity, record);
        return toRecord(jpaRepository.save(entity));
    }

    @Override
    @Transactional
    public List<ErrorRecord> saveAll(List<ErrorRecord> records) {
        List<ErrorRecordEntity> entities = records.stream().map(record -> {
            ErrorRecordEntity entity = record.id() == null
                    ? new ErrorRecordEntity()
                    : jpaRepository.findById(record.id()).orElseGet(ErrorRecordEntity::new);
            applyRecord(entity, record);
            return entity;
        }).toList();
        return jpaRepository.saveAll(entities).stream().map(this::toRecord).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ErrorRecord> findByOwnerUserId(Long ownerUserId) {
        return jpaRepository.findByOwnerUserId(ownerUserId).stream().map(this::toRecord).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ErrorRecord> findAll() {
        return jpaRepository.findAll().stream().map(this::toRecord).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ErrorRecord> findById(Long id) {
        return jpaRepository.findById(id).map(this::toRecord);
    }

    @Override
    @Transactional
    public ErrorRecord markMastered(Long errorId, boolean mastered) {
        ErrorRecordEntity entity = jpaRepository.findById(errorId)
                .orElseThrow(() -> new IllegalArgumentException("error record not found: " + errorId));
        entity.setMastered(mastered);
        return toRecord(jpaRepository.save(entity));
    }

    private void applyRecord(ErrorRecordEntity entity, ErrorRecord record) {
        entity.setOwnerUserId(record.ownerUserId());
        entity.setSourceFileId(record.sourceFileId());
        entity.setChapter(record.chapter());
        entity.setCategory(record.category());
        entity.setErrorType(record.errorType());
        entity.setErrorCode(record.errorCode());
        entity.setFixSuggestion(record.fixSuggestion());
        entity.setErrorLines(joinLines(record.line()));
        entity.setCreatedAt(record.createdAt());
        entity.setMastered(record.mastered());
    }

    private String joinLines(List<Integer> lines) {
        if (lines == null || lines.isEmpty()) {
            return null;
        }
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < lines.size(); i++) {
            if (i > 0) {
                builder.append(',');
            }
            builder.append(lines.get(i));
        }
        return builder.toString();
    }

    private List<Integer> splitLines(String stored) {
        if (stored == null || stored.isBlank()) {
            return List.of();
        }
        return Arrays.stream(stored.split(","))
                .map(String::trim)
                .filter(part -> !part.isEmpty())
                .map(Integer::valueOf)
                .toList();
    }

    private ErrorRecord toRecord(ErrorRecordEntity entity) {
        return new ErrorRecord(
                entity.getId(),
                entity.getOwnerUserId(),
                entity.getSourceFileId(),
                entity.getChapter(),
                entity.getCategory(),
                entity.getErrorType(),
                entity.getErrorCode(),
                entity.getFixSuggestion(),
                splitLines(entity.getErrorLines()),
                entity.getCreatedAt(),
                entity.isMastered());
    }
}
