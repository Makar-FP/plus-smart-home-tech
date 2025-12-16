CREATE TABLE IF NOT EXISTS products (
    id UUID PRIMARY KEY,
    weight NUMERIC,
    depth NUMERIC,
    height NUMERIC,
    width NUMERIC,
    fragile BOOLEAN NOT NULL,
    quantity BIGINT
);