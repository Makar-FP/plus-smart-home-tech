package ru.yandex.practicum.commerce.warehouse.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.commerce.interactionapi.dto.*;
import ru.yandex.practicum.commerce.interactionapi.operation.WarehouseOperation;
import ru.yandex.practicum.commerce.warehouse.service.WarehouseService;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/api/v1/warehouse")
public class WarehouseController implements WarehouseOperation {

    private final WarehouseService warehouseService;

    @Override
    public WarehouseDto newProductInWarehouse(@RequestBody NewProductInWarehouseRequest request) {
        log.info("--> newProductInWarehouse: {}", request);
        WarehouseDto result = warehouseService.newProductInWarehouse(request);
        log.info("<-- newProductInWarehouse: {}", result);
        return result;
    }

    @Override
    public WarehouseDto addProductToWarehouse(@RequestBody AddProductToWarehouseRequest request) {
        log.info("--> addProductToWarehouse: {}", request);
        WarehouseDto result = warehouseService.addProductToWarehouse(request);
        log.info("<-- addProductToWarehouse: {}", result);
        return result;
    }

    @Override
    public AddressDto getWarehouseAddress() {
        log.info("--> getWarehouseAddress");
        AddressDto result = warehouseService.getWarehouseAddress();
        log.info("<-- getWarehouseAddress: {}", result);
        return result;
    }

    @Override
    public BookedProductsDto checkProductQuantityEnoughForShoppingCart(@RequestBody ShoppingCartDto request) {
        log.info("--> check cart: {}", request);
        BookedProductsDto result = warehouseService.checkProductQuantityEnoughForShoppingCart(request);
        log.info("<-- check result: {}", result);
        return result;
    }
}