package com.mylab.ailearn.core.repository;

import com.mylab.ailearn.core.enums.CourseChapter;
import com.mylab.ailearn.core.enums.ProgrammingLanguage;
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
 * source_file 表的 JPA 实体，对应 core 模型的 {@code SourceFile} 记录。
 * 仅供 SPI 适配器使用，Service 层不直接依赖本类。
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "source_file")
public class SourceFileEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "owner_user_id", nullable = false)
    private Long ownerUserId;

    @Column(name = "filename", nullable = false)
    private String filename;

    @Enumerated(EnumType.STRING)
    @Column(name = "language", nullable = false, length = 20)
    private ProgrammingLanguage language;

    @Enumerated(EnumType.STRING)
    @Column(name = "chapter", length = 30)
    private CourseChapter chapter;

    @Column(name = "content", nullable = false, length = 262144)
    private String content;

    @Column(name = "submitted_at")
    private LocalDateTime submittedAt;
}
