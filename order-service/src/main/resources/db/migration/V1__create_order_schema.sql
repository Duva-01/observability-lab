CREATE TABLE orders (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    status VARCHAR(30) NOT NULL,
    total NUMERIC(10, 2) NOT NULL
);

CREATE TABLE order_items (
    id BIGSERIAL PRIMARY KEY,
    order_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    quantity INTEGER NOT NULL,
    CONSTRAINT fk_order_items_order
        FOREIGN KEY (order_id) REFERENCES orders (id)
);

INSERT INTO orders (id, user_id, status, total) VALUES
    (5001, 1, 'CREATED', 13.30),
    (5002, 2, 'CONFIRMED', 1.79),
    (5003, 3, 'DELIVERED', 6.14);

INSERT INTO order_items (id, order_id, product_id, quantity) VALUES
    (1, 5001, 101, 1),
    (2, 5001, 102, 1),
    (3, 5002, 103, 1),
    (4, 5003, 102, 1),
    (5, 5003, 103, 1);

SELECT setval('orders_id_seq', (SELECT MAX(id) FROM orders));
SELECT setval('order_items_id_seq', (SELECT MAX(id) FROM order_items));
