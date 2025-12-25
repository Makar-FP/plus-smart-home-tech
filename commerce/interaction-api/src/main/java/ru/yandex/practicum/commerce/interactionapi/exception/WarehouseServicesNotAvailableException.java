package ru.yandex.practicum.commerce.interactionapi.exception;

public class WarehouseServicesNotAvailableException extends RuntimeException{
    public WarehouseServicesNotAvailableException() {
        super("Service warehouse is not available now");
    }
}
