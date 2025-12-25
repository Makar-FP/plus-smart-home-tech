package ru.yandex.practicum.commerce.interactionapi.client;

import feign.RetryableException;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.commerce.interactionapi.dto.common.AddressDto;
import ru.yandex.practicum.commerce.interactionapi.dto.common.BookedProductsDto;
import ru.yandex.practicum.commerce.interactionapi.dto.cart.ShoppingCartDto;
import ru.yandex.practicum.commerce.interactionapi.dto.warehouse.*;
import ru.yandex.practicum.commerce.interactionapi.exception.WarehouseServicesNotAvailableException;

@Component
public class DefaultWarehouseOperation implements WarehouseClient {

    @Override
    public AddressDto getWarehouseAddress() {
        throw new WarehouseServicesNotAvailableException();
    }

    @Override
    public BookedProductsDto checkProductQuantityEnoughForShoppingCart(ShoppingCartDto request) throws RetryableException {
        throw new WarehouseServicesNotAvailableException();
    }

    @Override
    public void shippedToDelivery(ShippedToDeliveryRequest request) {
        throw new WarehouseServicesNotAvailableException();
    }

    @Override
    public BookedProductsDto assemblyProductsForOrder(AssemblyProductsForOrderRequest request) {
        throw new WarehouseServicesNotAvailableException();
    }
}
