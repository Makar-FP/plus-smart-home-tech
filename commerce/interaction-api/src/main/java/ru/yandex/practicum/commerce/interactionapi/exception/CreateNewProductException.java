package ru.yandex.practicum.commerce.interactionapi.exception;

import ru.yandex.practicum.commerce.interactionapi.dto.store.ProductDto;

public class CreateNewProductException extends RuntimeException {
    public CreateNewProductException(ProductDto product) {
        super("Product creating" + product + " returned an exception");
    }
}
