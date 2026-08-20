ALTER TABLE profile
    ADD COLUMN username VARCHAR(50) NULL AFTER user_id,
    ADD COLUMN role VARCHAR(20) NULL AFTER username;

-- 先补齐已有档案，再为没有档案的历史用户创建档案，避免唯一外键冲突。
UPDATE profile p
    INNER JOIN app_user u ON u.id = p.user_id
SET p.username = CONCAT('user_', u.id),
    p.role = u.role;

INSERT INTO profile (user_id, username, role)
SELECT u.id, CONCAT('user_', u.id), u.role
FROM app_user u
         LEFT JOIN profile p ON p.user_id = u.id
WHERE p.id IS NULL;

ALTER TABLE profile
    MODIFY COLUMN username VARCHAR(50) NOT NULL,
    MODIFY COLUMN role VARCHAR(20) NOT NULL,
    ADD CONSTRAINT uk_profile_username UNIQUE (username);

ALTER TABLE app_user
    DROP COLUMN role;
