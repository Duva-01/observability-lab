CREATE TABLE products (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(120) NOT NULL,
    category VARCHAR(80) NOT NULL,
    price NUMERIC(10, 2) NOT NULL
);

INSERT INTO products (id, name, category, price) VALUES
    (101, 'Aceite de oliva', 'Despensa', 8.95),
    (102, 'Cafe molido', 'Desayuno', 4.35),
    (103, 'Pasta integral', 'Despensa', 1.79);

SELECT setval('products_id_seq', (SELECT MAX(id) FROM products));
