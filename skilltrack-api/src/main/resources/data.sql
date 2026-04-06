-- Initial Roles
INSERT INTO roles (name) SELECT 'ROLE_STUDENT' WHERE NOT EXISTS (SELECT 1 FROM roles WHERE name='ROLE_STUDENT');
INSERT INTO roles (name) SELECT 'ROLE_INSTRUCTOR' WHERE NOT EXISTS (SELECT 1 FROM roles WHERE name='ROLE_INSTRUCTOR');
INSERT INTO roles (name) SELECT 'ROLE_ADMIN' WHERE NOT EXISTS (SELECT 1 FROM roles WHERE name='ROLE_ADMIN');

-- Note: Admin user creation is still handled by AdminBootstrapConfig if needed,
-- but having it here ensures a clean slate if bootstrap is disabled.
-- The password below is 'admin123' encoded with BCrypt.
INSERT INTO users (username, email, password, enabled, deleted, roles_csv, created_at)
SELECT 'admin', 'admin@skilltrack.com', '$2a$10$8.UnVuG9HHvBv3JTy0aO/eXwV4nIe7L3F7E7J7T7h7l7i7n7g7S7', true, false, 'ROLE_ADMIN,ROLE_INSTRUCTOR,ROLE_STUDENT', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM users WHERE username='admin');

-- Link Admin to Roles
INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id FROM users u, roles r
WHERE u.username='admin' AND r.name IN ('ROLE_ADMIN', 'ROLE_INSTRUCTOR', 'ROLE_STUDENT')
AND NOT EXISTS (SELECT 1 FROM user_roles ur WHERE ur.user_id=u.id AND ur.role_id=r.id);

-- Collection table entries for roles
INSERT INTO user_role_names (user_id, role_name)
SELECT u.id, 'ROLE_ADMIN' FROM users u WHERE u.username='admin' AND NOT EXISTS (SELECT 1 FROM user_role_names urn WHERE urn.user_id=u.id AND urn.role_name='ROLE_ADMIN');
INSERT INTO user_role_names (user_id, role_name)
SELECT u.id, 'ROLE_INSTRUCTOR' FROM users u WHERE u.username='admin' AND NOT EXISTS (SELECT 1 FROM user_role_names urn WHERE urn.user_id=u.id AND urn.role_name='ROLE_INSTRUCTOR');
INSERT INTO user_role_names (user_id, role_name)
SELECT u.id, 'ROLE_STUDENT' FROM users u WHERE u.username='admin' AND NOT EXISTS (SELECT 1 FROM user_role_names urn WHERE urn.user_id=u.id AND urn.role_name='ROLE_STUDENT');
