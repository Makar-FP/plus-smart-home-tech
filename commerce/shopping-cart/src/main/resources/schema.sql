CREATE TABLE IF NOT EXISTS shopping_carts (
    id UUID NOT NULL,
    username VARCHAR NOT NULL,
    product_id UUID NOT NULL,
    quantity BIGINT,
    CONSTRAINT PK_CART PRIMARY KEY (id, product_id)
);