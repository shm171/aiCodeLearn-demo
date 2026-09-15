package com.mylab.ailearn.core.repository;

import com.mylab.ailearn.core.enums.CourseChapter;
import com.mylab.ailearn.core.enums.ErrorCategory;
import com.mylab.ailearn.core.model.commonmodel.ErrorRecord;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ErrorRecordStoreImplTest {

    private final ErrorRecordJpaRepository repository = mock(ErrorRecordJpaRepository.class);
    private final ErrorRecordStoreImpl store = new ErrorRecordStoreImpl(repository);

    @Test
    void markingUnknownErrorDoesNotCreateANewRow() {
        when(repository.findById(88L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> store.markMastered(88L, true))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("error record not found: 88");
        verify(repository, never()).save(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void archiveUsesStableNewestFirstRepositoryQuery() {
        ErrorRecordEntity newer = entity(2L, LocalDateTime.of(2026, 9, 14, 12, 0));
        ErrorRecordEntity older = entity(1L, LocalDateTime.of(2026, 9, 13, 12, 0));
        when(repository.findByOwnerUserIdOrderByCreatedAtDescIdDesc(7L))
                .thenReturn(List.of(newer, older));

        List<ErrorRecord> result = store.findByOwnerUserId(7L);

        assertThat(result).extracting(ErrorRecord::id).containsExactly(2L, 1L);
        verify(repository).findByOwnerUserIdOrderByCreatedAtDescIdDesc(7L);
    }

    private ErrorRecordEntity entity(Long id, LocalDateTime createdAt) {
        ErrorRecordEntity entity = new ErrorRecordEntity();
        entity.setId(id);
        entity.setOwnerUserId(7L);
        entity.setSourceFileId(20L);
        entity.setChapter(CourseChapter.ARRAYS);
        entity.setCategory(ErrorCategory.LOGIC_ERROR);
        entity.setErrorType("array access");
        entity.setErrorCode("ARRAY_OUT_OF_BOUNDS");
        entity.setFixSuggestion("check bounds");
        entity.setErrorLines("3,7");
        entity.setCreatedAt(createdAt);
        entity.setMastered(false);
        return entity;
    }
}
