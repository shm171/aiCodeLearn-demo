CREATE TABLE error_record
(
    id             BIGINT       NOT NULL AUTO_INCREMENT,
    owner_user_id  BIGINT       NOT NULL,
    source_file_id BIGINT       NULL,
    chapter        VARCHAR(30)  NULL,
    category       VARCHAR(20)  NULL,
    error_type     VARCHAR(255) NULL,
    error_code     VARCHAR(100) NULL,
    fix_suggestion VARCHAR(1000) NULL,
    error_lines    VARCHAR(255) NULL,
    created_at     TIMESTAMP    NULL,
    mastered       TINYINT(1)   NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    INDEX idx_error_record_owner (owner_user_id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;
