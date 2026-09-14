package com.mylab.ailearn.core.repository;

import com.mylab.ailearn.core.enums.CourseChapter;
import com.mylab.ailearn.core.enums.ProgrammingLanguage;
import com.mylab.ailearn.core.model.commonmodel.SourceFile;
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

class SourceFileStoreImplTest {

    private final SourceFileJpaRepository repository = mock(SourceFileJpaRepository.class);
    private final SourceFileStoreImpl store = new SourceFileStoreImpl(repository);

    @Test
    void updatingUnknownSourceFileDoesNotCreateANewRow() {
        SourceFile sourceFile = new SourceFile(
                99L,
                7L,
                "Main.java",
                ProgrammingLanguage.JAVA,
                CourseChapter.FUNDAMENTALS,
                "class Main {}",
                LocalDateTime.of(2026, 9, 14, 12, 0));
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> store.save(sourceFile))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("source file not found: 99");
        verify(repository, never()).save(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void ownerHistoryUsesStableNewestFirstRepositoryQuery() {
        SourceFileEntity newer = entity(2L, LocalDateTime.of(2026, 9, 14, 12, 0));
        SourceFileEntity older = entity(1L, LocalDateTime.of(2026, 9, 13, 12, 0));
        when(repository.findByOwnerUserIdOrderBySubmittedAtDescIdDesc(7L))
                .thenReturn(List.of(newer, older));

        List<SourceFile> result = store.findByOwnerUserId(7L);

        assertThat(result).extracting(SourceFile::id).containsExactly(2L, 1L);
        verify(repository).findByOwnerUserIdOrderBySubmittedAtDescIdDesc(7L);
    }

    private SourceFileEntity entity(Long id, LocalDateTime submittedAt) {
        SourceFileEntity entity = new SourceFileEntity();
        entity.setId(id);
        entity.setOwnerUserId(7L);
        entity.setFilename("Main.java");
        entity.setLanguage(ProgrammingLanguage.JAVA);
        entity.setChapter(CourseChapter.FUNDAMENTALS);
        entity.setContent("class Main {}");
        entity.setSubmittedAt(submittedAt);
        return entity;
    }
}
