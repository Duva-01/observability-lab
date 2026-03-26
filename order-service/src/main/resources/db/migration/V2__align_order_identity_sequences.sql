CREATE SEQUENCE IF NOT EXISTS orders_id_seq;
ALTER SEQUENCE orders_id_seq OWNED BY orders.id;
ALTER TABLE orders ALTER COLUMN id SET DEFAULT nextval('orders_id_seq');
SELECT setval('orders_id_seq', COALESCE((SELECT MAX(id) FROM orders), 1));

CREATE SEQUENCE IF NOT EXISTS order_items_id_seq;
ALTER SEQUENCE order_items_id_seq OWNED BY order_items.id;
ALTER TABLE order_items ALTER COLUMN id SET DEFAULT nextval('order_items_id_seq');
SELECT setval('order_items_id_seq', COALESCE((SELECT MAX(id) FROM order_items), 1));
