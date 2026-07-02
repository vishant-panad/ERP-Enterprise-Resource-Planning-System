USE auth_db;

-- 1. Disable Safety Lock
SET SQL_SAFE_UPDATES = 0;

-- 2. Clear old users
DELETE FROM users_auth;

-- 3. Insert users (with the verified hash)
INSERT INTO users_auth (user_id, username, role, password_hash, status) VALUES
(1001, 'admin1', 'ADMIN',      '$2a$10$bZj.FWbkJRkPmSdaPiZDD.4xrzGJsRoTLa8rOh2P/dFS3kLCjWV1i', 'ACTIVE'),
(1002, 'inst1',  'INSTRUCTOR', '$2a$10$bZj.FWbkJRkPmSdaPiZDD.4xrzGJsRoTLa8rOh2P/dFS3kLCjWV1i', 'ACTIVE'),
(1003, 'stu1',   'STUDENT',    '$2a$10$bZj.FWbkJRkPmSdaPiZDD.4xrzGJsRoTLa8rOh2P/dFS3kLCjWV1i', 'ACTIVE'),
(1004, 'stu2',   'STUDENT',    '$2a$10$bZj.FWbkJRkPmSdaPiZDD.4xrzGJsRoTLa8rOh2P/dFS3kLCjWV1i', 'ACTIVE');

-- 4. Re-enable Safety Lock
SET SQL_SAFE_UPDATES = 1;