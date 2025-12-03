package ru.yandex.practicum.commerce.interactionapi.exception;

import java.util.UUID;

public class ProductInShoppingCartLowQuantityInWarehouse extends RuntimeException{
    public ProductInShoppingCartLowQuantityInWarehouse(UUID productId) {
        super("Product " + productId + " from the shopping cart is not in the required quantity in warehouse");
    }
}
