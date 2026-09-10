package com.mylab.ailearn.core.service;

import com.mylab.ailearn.core.model.commonmodel.ErrorRecord;
import com.mylab.ailearn.core.model.commonmodel.GradedError;
import com.mylab.ailearn.core.model.commonmodel.GradingResult;
import com.mylab.ailearn.core.model.commonmodel.SourceFile;
import com.mylab.ailearn.core.service.spi.ErrorRecordStore;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 错误记录的默认实现：把批改出的错误转换为 ErrorRecord 落库。
 */
@Service
@RequiredArgsConstructor
public class ErrorRecordServiceImpl implements ErrorRecordService {

    private final ObjectProvider<ErrorRecordStore> errorRecordStoreProvider;

    // 记录一次批改产生的全部错误。
    @Override
    @Transactional
    public List<ErrorRecord> record(Long ownerUserId, SourceFile sourceFile, GradingResult gradingResult) {
        ServiceSupport.requireOwner(ownerUserId);
        if (sourceFile == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "sourceFile 不能为空");
        }
        if (gradingResult == null || gradingResult.errors().isEmpty()) {
            return List.of();
        }

        LocalDateTime now = LocalDateTime.now();
        List<ErrorRecord> records = gradingResult.errors().stream()
                .map(error -> toRecord(ownerUserId, sourceFile, error, now))
                .toList();

        return List.copyOf(ServiceSupport.nullToEmpty(store().saveAll(records)));
    }

    private ErrorRecord toRecord(Long ownerUserId, SourceFile sourceFile, GradedError error, LocalDateTime now) {
        return new ErrorRecord(
                null,
                ownerUserId,
                sourceFile.id(),
                sourceFile.chapter(),
                error.category(),
                error.errorType(),
                error.errorCode(),
                error.fixSuggestion(),
                error.line(),
                now,
                false);
    }

    private ErrorRecordStore store() {
        return ServiceSupport.required(errorRecordStoreProvider, "ErrorRecordStore");
    }
}
