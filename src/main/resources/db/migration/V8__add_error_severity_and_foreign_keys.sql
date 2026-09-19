ALTER TABLE error_record
    ADD COLUMN severity VARCHAR(10) NULL AFTER category;

UPDATE error_record
SET severity = CASE category
    WHEN 'SYNTAX_ERROR' THEN 'ERROR'
    WHEN 'LOGIC_ERROR' THEN 'WARNING'
    ELSE 'INFO'
END
WHERE severity IS NULL;

ALTER TABLE error_record
    MODIFY COLUMN severity VARCHAR(10) NOT NULL,
    ADD INDEX idx_error_record_owner_severity (owner_user_id, severity),
    ADD CONSTRAINT fk_error_record_owner FOREIGN KEY (owner_user_id)
        REFERENCES app_user (id) ON DELETE CASCADE,
    ADD CONSTRAINT fk_error_record_source FOREIGN KEY (source_file_id)
        REFERENCES source_file (id) ON DELETE SET NULL;

ALTER TABLE source_file
    ADD CONSTRAINT fk_source_file_owner FOREIGN KEY (owner_user_id)
        REFERENCES app_user (id) ON DELETE CASCADE;
