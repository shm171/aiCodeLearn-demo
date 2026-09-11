CREATE TABLE source_file
(
    id            BIGINT      NOT NULL AUTO_INCREMENT,
    owner_user_id BIGINT      NOT NULL,
    filename      VARCHAR(255) NOT NULL,
    language      VARCHAR(20) NOT NULL,
    chapter       VARCHAR(30) NULL,
    content       MEDIUMTEXT  NOT NULL,
    submitted_at  TIMESTAMP   NULL,
    PRIMARY KEY (id),
    INDEX idx_source_file_owner (owner_user_id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;
