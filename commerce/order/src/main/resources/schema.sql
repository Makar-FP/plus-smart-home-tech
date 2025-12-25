CREATE TABLE IF NOT EXISTS orders (
    id UUID PRIMARY KEY,
    username VARCHAR,
    cart_id UUID,
    payment_id UUID,
    delivery_id UUID,
    state VARCHAR,
    delivery_weight NUMERIC,
    delivery_volume NUMERIC,
    product_category VARCHAR,
    fragile BOOLEAN,
    total_price NUMERIC,
    deliver_price NUMERIC,
    product_price NUMERIC,
    address_id UUID
);

CREATE TABLE IF NOT EXISTS products (
    id UUID PRIMARY KEY,
    product_id UUID,
    order_id UUID,
    quantity BIGINT
);

CREATE TABLE IF NOT EXISTS addresses (
    id UUID PRIMARY KEY,
    country VARCHAR,
    city VARCHAR,
    street VARCHAR,
    house VARCHAR,
    flat VARCHAR
);