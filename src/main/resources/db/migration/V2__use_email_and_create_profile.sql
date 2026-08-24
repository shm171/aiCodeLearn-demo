ALTER TABLE app_user
    DROP INDEX uk_app_user_username,
    CHANGE COLUMN username email VARCHAR(254) NOT NULL,
    ADD CONSTRAINT uk_app_user_email UNIQUE (email);

CREATE TABLE profile
(
    id         BIGINT    NOT NULL AUTO_INCREMENT,
    user_id    BIGINT    NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT uk_profile_user_id UNIQUE (user_id),
    CONSTRAINT fk_profile_app_user FOREIGN KEY (user_id)
        REFERENCES app_user (id) ON DELETE CASCADE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;
