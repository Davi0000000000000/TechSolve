-- Inserir usuários padrão
INSERT INTO users (username, email, password)
VALUES 
('user1', 'user1@example.com', '$2a$10$Dn9PZOlmr1F4PXHcFJYEXOdvU0X2kQ9qXxHnFS2K7l5hBgldO4SUO'), -- senha = 123456
('admin', 'admin@example.com', '$2a$10$Dn9PZOlmr1F4PXHcFJYEXOdvU0X2kQ9qXxHnFS2K7l5hBgldO4SUO'), -- senha = 123456
('user2', 'user2@example.com', '$2a$10$Dn9PZOlmr1F4PXHcFJYEXOdvU0X2kQ9qXxHnFS2K7l5hBgldO4SUO'), -- senha = 123456
('user3', 'user3@example.com', '$2a$10$Dn9PZOlmr1F4PXHcFJYEXOdvU0X2kQ9qXxHnFS2K7l5hBgldO4SUO'); -- senha = 123456

-- Atribuir roles aos usuários
INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id
FROM users u, role r
WHERE u.username = 'user1' AND r.role_name = 'USER';

INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id
FROM users u, role r
WHERE u.username = 'admin' AND r.role_name = 'ADMIN';

INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id
FROM users u, role r
WHERE u.username = 'user2' AND r.role_name = 'USER';

INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id
FROM users u, role r
WHERE u.username = 'user3' AND r.role_name = 'USER';

-- Inserir tarefas para user1
INSERT INTO task (title, description, due_date, completed, created_at, category, priority, user_id)
VALUES 
('Task 1', 'Description for Task 1', NOW() + INTERVAL 2 DAY, false, NOW(), 'WORK', 'HIGH', (SELECT id FROM users WHERE username = 'user1')),
('Task 2', 'Description for Task 2', NOW() + INTERVAL 5 DAY, false, NOW(), 'STUDY', 'MEDIUM', (SELECT id FROM users WHERE username = 'user1'));

-- Inserir tarefa para admin
INSERT INTO task (title, description, due_date, completed, created_at, category, priority, user_id)
VALUES 
('Admin Task', 'Important task for admin', NOW() + INTERVAL 7 DAY, false, NOW(), 'MANAGEMENT', 'HIGH', (SELECT id FROM users WHERE username = 'admin'));

-- Inserir 10 tarefas para user2
INSERT INTO task (title, description, due_date, completed, created_at, category, priority, user_id)
VALUES 
('User2 Task 1', 'Description for User2 Task 1', NOW() + INTERVAL 1 DAY, false, NOW(), 'WORK', 'HIGH', (SELECT id FROM users WHERE username = 'user2')),
('User2 Task 2', 'Description for User2 Task 2', NOW() + INTERVAL 2 DAY, false, NOW(), 'STUDY', 'MEDIUM', (SELECT id FROM users WHERE username = 'user2')),
('User2 Task 3', 'Description for User2 Task 3', NOW() + INTERVAL 3 DAY, false, NOW(), 'HOME', 'LOW', (SELECT id FROM users WHERE username = 'user2')),
('User2 Task 4', 'Description for User2 Task 4', NOW() + INTERVAL 4 DAY, false, NOW(), 'PERSONAL', 'LOW', (SELECT id FROM users WHERE username = 'user2')),
('User2 Task 5', 'Description for User2 Task 5', NOW() + INTERVAL 5 DAY, false, NOW(), 'HEALTH', 'HIGH', (SELECT id FROM users WHERE username = 'user2')),
('User2 Task 6', 'Description for User2 Task 6', NOW() + INTERVAL 6 DAY, false, NOW(), 'WORK', 'HIGH', (SELECT id FROM users WHERE username = 'user2')),
('User2 Task 7', 'Description for User2 Task 7', NOW() + INTERVAL 7 DAY, false, NOW(), 'STUDY', 'MEDIUM', (SELECT id FROM users WHERE username = 'user2')),
('User2 Task 8', 'Description for User2 Task 8', NOW() + INTERVAL 8 DAY, false, NOW(), 'HOME', 'LOW', (SELECT id FROM users WHERE username = 'user2')),
('User2 Task 9', 'Description for User2 Task 9', NOW() + INTERVAL 9 DAY, false, NOW(), 'PERSONAL', 'LOW', (SELECT id FROM users WHERE username = 'user2')),
('User2 Task 10', 'Description for User2 Task 10', NOW() + INTERVAL 10 DAY, false, NOW(), 'HEALTH', 'HIGH', (SELECT id FROM users WHERE username = 'user2'));

-- Inserir 10 tarefas para user3
INSERT INTO task (title, description, due_date, completed, created_at, category, priority, user_id)
VALUES 
('User3 Task 1', 'Description for User3 Task 1', NOW() + INTERVAL 1 DAY, false, NOW(), 'WORK', 'HIGH', (SELECT id FROM users WHERE username = 'user3')),
('User3 Task 2', 'Description for User3 Task 2', NOW() + INTERVAL 2 DAY, false, NOW(), 'STUDY', 'MEDIUM', (SELECT id FROM users WHERE username = 'user3')),
('User3 Task 3', 'Description for User3 Task 3', NOW() + INTERVAL 3 DAY, false, NOW(), 'HOME', 'LOW', (SELECT id FROM users WHERE username = 'user3')),
('User3 Task 4', 'Description for User3 Task 4', NOW() + INTERVAL 4 DAY, false, NOW(), 'PERSONAL', 'LOW', (SELECT id FROM users WHERE username = 'user3')),
('User3 Task 5', 'Description for User3 Task 5', NOW() + INTERVAL 5 DAY, false, NOW(), 'HEALTH', 'HIGH', (SELECT id FROM users WHERE username = 'user3')),
('User3 Task 6', 'Description for User3 Task 6', NOW() + INTERVAL 6 DAY, false, NOW(), 'WORK', 'HIGH', (SELECT id FROM users WHERE username = 'user3')),
('User3 Task 7', 'Description for User3 Task 7', NOW() + INTERVAL 7 DAY, false, NOW(), 'STUDY', 'MEDIUM', (SELECT id FROM users WHERE username = 'user3')),
('User3 Task 8', 'Description for User3 Task 8', NOW() + INTERVAL 8 DAY, false, NOW(), 'HOME', 'LOW', (SELECT id FROM users WHERE username = 'user3')),
('User3 Task 9', 'Description for User3 Task 9', NOW() + INTERVAL 9 DAY, false, NOW(), 'PERSONAL', 'LOW', (SELECT id FROM users WHERE username = 'user3')),
('User3 Task 10', 'Description for User3 Task 10', NOW() + INTERVAL 10 DAY, false, NOW(), 'HEALTH', 'HIGH', (SELECT id FROM users WHERE username = 'user3'));
