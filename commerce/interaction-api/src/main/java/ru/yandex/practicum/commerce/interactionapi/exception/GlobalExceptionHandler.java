package ru.yandex.practicum.commerce.interactionapi.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(SpecifiedProductAlreadyInWarehouseException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ExceptionResponse handleSpecifiedProductAlreadyInWarehouseException(SpecifiedProductAlreadyInWarehouseException ex) {
        return new ExceptionResponse(
                HttpStatus.BAD_REQUEST.name(),
                "Bad request.",
                ex.getMessage()
        );
    }

    @ExceptionHandler(CreateNewProductException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ExceptionResponse handleNewProductCreatingException(CreateNewProductSericeException ex) {
        log.error("500 Internal Server Error: {}", ex.getMessage(), ex);
        return new ExceptionResponse(
                HttpStatus.INTERNAL_SERVER_ERROR.name(),
                "SERVICE-ERROR.",
                ex.getMessage()
        );
    }

    @ExceptionHandler(WarehouseServicesNotAvailableException.class)
    @ResponseStatus(HttpStatus.METHOD_NOT_ALLOWED)
    public ExceptionResponse handleWarehouseServicesNotAvailableException(WarehouseServicesNotAvailableException ex) {
        return new ExceptionResponse(
                HttpStatus.METHOD_NOT_ALLOWED.name(),
                "Service not available.",
                ex.getMessage()
        );
    }

    @ExceptionHandler(NoDeliveryFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ExceptionResponse handleNoDeliveryFoundException(NoDeliveryFoundException ex) {
        return new ExceptionResponse(
                HttpStatus.NOT_FOUND.name(),
                "The required payment was not found.",
                ex.getMessage()
        );
    }

    @ExceptionHandler(NoPaymentFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ExceptionResponse handleNoPaymentFoundException(NoPaymentFoundException ex) {
        return new ExceptionResponse(
                HttpStatus.NOT_FOUND.name(),
                "The required payment was not found.",
                ex.getMessage()
        );
    }


    @ExceptionHandler(NoOrderFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ExceptionResponse handleNoOrderFoundException(NoOrderFoundException ex) {
        return new ExceptionResponse(
                HttpStatus.NOT_FOUND.name(),
                "The required order was not found.",
                ex.getMessage()
        );
    }

    @ExceptionHandler(NoProductsInShoppingCartException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ExceptionResponse handleProductsInShoppingCartNotFound(NoProductsInShoppingCartException ex) {
        return new ExceptionResponse(
                HttpStatus.NOT_FOUND.name(),
                "The required object was not found.",
                ex.getMessage()
        );
    }

    @ExceptionHandler(NoSpecifiedProductInWarehouseException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ExceptionResponse handleWarehouseProductNotFound(NoSpecifiedProductInWarehouseException ex) {
        return new ExceptionResponse(
                HttpStatus.NOT_FOUND.name(),
                "The required object was not found.",
                ex.getMessage()
        );
    }

    @ExceptionHandler(ProductNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ExceptionResponse handleProductNotFound(ProductNotFoundException ex) {
        return new ExceptionResponse(
                HttpStatus.NOT_FOUND.name(),
                "The required object was not found.",
                ex.getMessage()
        );
    }

    @ExceptionHandler(NotAuthorizedUserException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ExceptionResponse handleUserNotAuthorized(NotAuthorizedUserException ex) {
        return new ExceptionResponse(
                HttpStatus.UNAUTHORIZED.name(),
                "User not authorized.",
                ex.getMessage()
        );
    }

    @ExceptionHandler(CreateNewProductSericeException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ExceptionResponse handleNewProductServiceException(CreateNewProductSericeException ex) {
        log.error("500 Internal Server Error (CreateNewProductSericeException): {}", ex.getMessage(), ex);

        return new ExceptionResponse(
                HttpStatus.INTERNAL_SERVER_ERROR.name(),
                "Service error.",
                "Unexpected error. Please try again later."
        );
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ExceptionResponse handleException(Exception ex) {
        log.error("500 Internal Server Error: {}", ex.getMessage(), ex);

        return new ExceptionResponse(
                HttpStatus.INTERNAL_SERVER_ERROR.name(),
                "Error occurred.",
                "Unexpected error. Please try again later."
        );
    }
}