CREATE TABLE IF NOT EXISTS payments (
    id UUID PRIMARY KEY,
    order_id UUID,
    total_price NUMERIC,
    delivery_price NUMERIC,
    product_price NUMERIC,
    payment_status VARCHAR
);