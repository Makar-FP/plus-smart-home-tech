CREATE TABLE IF NOT EXISTS deliveries (
    id UUID PRIMARY KEY,
    delivery_weight NUMERIC,
    delivery_volume NUMERIC,
    fragile BOOLEAN,
    delivery_status VARCHAR,
    address_from_id UUID,
    address_to_id UUID,
    order_id UUID
);

CREATE TABLE IF NOT EXISTS addresses (
    id UUID PRIMARY KEY,
    country VARCHAR,
    city VARCHAR,
    street VARCHAR,
    house VARCHAR,
    flat VARCHAR
);