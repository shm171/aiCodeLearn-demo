package com.mylab.ailearn.core.repository;

import com.mylab.ailearn.core.enums.CourseChapter;
import com.mylab.ailearn.core.enums.ErrorCategory;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * error_record 表的 JPA 实体，对应 core 模型的 {@code ErrorRecord} 记录。
 * 出错行号列表以逗号分隔的字符串存储在 error_lines 列。
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "error_record")
public class ErrorRecordEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "owner_user_id", nullable = false)
    private Long ownerUserId;

    @Column(name = "source_file_id")
    private Long sourceFileId;

    @Enumerated(EnumType.STRING)
    @Column(name = "chapter", length = 30)
    private CourseChapter chapter;

    @Enumerated(EnumType.STRING)
    @Column(name = "category", length = 20)
    private ErrorCategory category;

    @Column(name = "error_type")
    private String errorType;

    @Column(name = "error_code", length = 100)
    private String errorCode;

    @Column(name = "fix_suggestion", length = 1000)
    private String fixSuggestion;

    @Column(name = "error_lines")
    private String errorLines;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "mastered", nullable = false)
    private boolean mastered;
}
