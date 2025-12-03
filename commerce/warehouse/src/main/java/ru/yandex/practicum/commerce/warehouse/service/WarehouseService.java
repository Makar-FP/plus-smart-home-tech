package ru.yandex.practicum.commerce.warehouse.service;

import ru.yandex.practicum.commerce.interactionapi.dto.*;

public interface WarehouseService {

    WarehouseDto newProductInWarehouse(NewProductInWarehouseRequest request);

    WarehouseDto addProductToWarehouse(AddProductToWarehouseRequest request);

    AddressDto getWarehouseAddress();

    BookedProductsDto checkProductQuantityEnoughForShoppingCart(ShoppingCartDto request);
}
