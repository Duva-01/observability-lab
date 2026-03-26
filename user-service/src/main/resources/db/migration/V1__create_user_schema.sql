CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(120) NOT NULL,
    email VARCHAR(180) NOT NULL UNIQUE,
    segment VARCHAR(30) NOT NULL
);

INSERT INTO users (id, name, email, segment) VALUES
    (1, 'Ana Lopez', 'ana.lopez@example.com', 'GOLD'),
    (2, 'Carlos Martin', 'carlos.martin@example.com', 'SILVER'),
    (3, 'Lucia Perez', 'lucia.perez@example.com', 'BRONZE');

SELECT setval('users_id_seq', (SELECT MAX(id) FROM users));
