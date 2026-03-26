CREATE SEQUENCE IF NOT EXISTS products_id_seq;
ALTER SEQUENCE products_id_seq OWNED BY products.id;
ALTER TABLE products ALTER COLUMN id SET DEFAULT nextval('products_id_seq');
SELECT setval('products_id_seq', COALESCE((SELECT MAX(id) FROM products), 1));
