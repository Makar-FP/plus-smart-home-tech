package ru.yandex.practicum.commerce.interactionapi.exception;

import java.util.UUID;

public class NoSpecifiedProductInWarehouseException extends RuntimeException {
    public NoSpecifiedProductInWarehouseException(UUID productId) {
        super("Product " + productId + " not found in warehouse");
    }
}
