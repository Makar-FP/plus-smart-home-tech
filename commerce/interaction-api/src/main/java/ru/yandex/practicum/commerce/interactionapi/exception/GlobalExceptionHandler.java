package ru.yandex.practicum.commerce.interactionapi.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(NoProductsInShoppingCartException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ExceptionResponse handleProductsInShoppingCartNotFound(NoProductsInShoppingCartException ex) {
        return new ExceptionResponse(
                HttpStatus.NOT_FOUND.name(),
                "The required object was not found.",
                ex.getMessage(),
                LocalDateTime.now()
        );
    }

    @ExceptionHandler(NoSpecifiedProductInWarehouseException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ExceptionResponse handleWarehouseProductNotFound(NoSpecifiedProductInWarehouseException ex) {
        return new ExceptionResponse(
                HttpStatus.NOT_FOUND.name(),
                "The required object was not found.",
                ex.getMessage(),
                LocalDateTime.now()
        );
    }

    @ExceptionHandler(ProductNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ExceptionResponse handleProductNotFound(ProductNotFoundException ex) {
        return new ExceptionResponse(
                HttpStatus.NOT_FOUND.name(),
                "The required object was not found.",
                ex.getMessage(),
                LocalDateTime.now()
        );
    }

    @ExceptionHandler(NotAuthorizedUserException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ExceptionResponse handleUserNotAuthorized(NotAuthorizedUserException ex) {
        return new ExceptionResponse(
                HttpStatus.UNAUTHORIZED.name(),
                "User not authorized.",
                ex.getMessage(),
                LocalDateTime.now()
        );
    }

    @ExceptionHandler(CreateNewProductSericeException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ExceptionResponse handleNewProductServiceException(CreateNewProductSericeException ex) {
        log.error("500 Internal Server Error (CreateNewProductSericeException): {}", ex.getMessage(), ex);

        return new ExceptionResponse(
                HttpStatus.INTERNAL_SERVER_ERROR.name(),
                "Service error.",
                "Unexpected error. Please try again later.",
                LocalDateTime.now()
        );
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ExceptionResponse handleException(Exception ex) {
        log.error("500 Internal Server Error: {}", ex.getMessage(), ex);

        return new ExceptionResponse(
                HttpStatus.INTERNAL_SERVER_ERROR.name(),
                "Error occurred.",
                "Unexpected error. Please try again later.",
                LocalDateTime.now()
        );
    }
}