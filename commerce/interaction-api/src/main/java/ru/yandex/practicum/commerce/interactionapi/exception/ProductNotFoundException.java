package ru.yandex.practicum.commerce.interactionapi.exception;

import java.util.UUID;

public class ProductNotFoundException extends RuntimeException{
    public ProductNotFoundException(UUID productId) {
        super("Product with id " + productId + "was not found");
    }
}
