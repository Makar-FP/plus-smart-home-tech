CREATE TABLE IF NOT EXISTS products (
    id UUID PRIMARY KEY,
    product_name VARCHAR,
    name VARCHAR,
    description VARCHAR,
    image_src VARCHAR,
    quantity_state VARCHAR,
    product_state VARCHAR,
    product_category VARCHAR,
    price NUMERIC
);