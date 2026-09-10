package com.mylab.ailearn.core.service;

import com.mylab.ailearn.core.model.commonmodel.ErrorRecord;
import com.mylab.ailearn.core.service.spi.ErrorRecordStore;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

/**
 * 错题归档的默认实现：从归档中查询某学生的错题。
 */
@Service
@RequiredArgsConstructor
public class ErrorArchiveServiceImpl implements ErrorArchiveService {

    private final ObjectProvider<ErrorRecordStore> errorRecordStoreProvider;

    /**
     * 查询某学生的全部错题。
     */
    @Override
    @Transactional(readOnly = true)
    public List<ErrorRecord> listByOwner(Long ownerUserId) {
        ServiceSupport.requireOwner(ownerUserId);
        return List.copyOf(ServiceSupport.nullToEmpty(store().findByOwnerUserId(ownerUserId)));
    }

    /**
     * 把某学生的一条错题标记为「已掌握」。归属校验（错题是否属于该学生）由实现完成。
     */
    @Override
    @Transactional
    public ErrorRecord markMastered(Long ownerUserId, Long errorId) {
        ServiceSupport.requireOwner(ownerUserId);
        if (errorId == null || errorId <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "errorId 无效");
        }
        ErrorRecord record = store().findById(errorId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "错题不存在"));
        if (!ownerUserId.equals(record.ownerUserId())) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "错题不存在");
        }
        return store().markMastered(errorId, true);
    }

    private ErrorRecordStore store() {
        return ServiceSupport.required(errorRecordStoreProvider, "ErrorRecordStore");
    }
}
