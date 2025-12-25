CREATE TABLE IF NOT EXISTS products (
    id UUID PRIMARY KEY,
    weight NUMERIC,
    depth NUMERIC,
    height NUMERIC,
    width NUMERIC,
    fragile BOOLEAN NOT NULL,
    quantity BIGINT
);

CREATE TABLE IF NOT EXISTS orders_booking (
    id UUID PRIMARY KEY,
    delivery_id UUID
);

CREATE TABLE IF NOT EXISTS booking_products (
    id UUID PRIMARY KEY,
    product_id UUID,
    order_id UUID,
    quantity BIGINT
);