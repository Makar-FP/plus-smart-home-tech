package ru.yandex.practicum.commerce.interactionapi.exception;

import ru.yandex.practicum.commerce.interactionapi.dto.ProductDto;

public class CreateNewProductSericeException extends RuntimeException {
    public CreateNewProductSericeException(ProductDto product) {
        super("An error has been occurred with product" + product + " during product creation");
    }
}
