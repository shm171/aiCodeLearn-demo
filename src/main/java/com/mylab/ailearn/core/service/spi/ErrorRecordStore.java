package com.mylab.ailearn.core.service.spi;

import com.mylab.ailearn.core.model.commonmodel.ErrorRecord;

import java.util.List;
import java.util.Optional;

/**
 * 错题归档持久化端口（SPI）。Repository 负责人实现后，错题归档与复习功能即可落库。
 */
public interface ErrorRecordStore {

    ErrorRecord save(ErrorRecord record);

    List<ErrorRecord> saveAll(List<ErrorRecord> records);

    List<ErrorRecord> findByOwnerUserId(Long ownerUserId);

    List<ErrorRecord> findAll();

    Optional<ErrorRecord> findById(Long id);

    /** 把指定错题的「已掌握」标记更新为 {@code mastered}，返回更新后的记录。 */
    ErrorRecord markMastered(Long errorId, boolean mastered);
}