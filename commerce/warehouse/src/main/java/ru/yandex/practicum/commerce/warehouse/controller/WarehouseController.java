package ru.yandex.practicum.commerce.warehouse.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.commerce.interactionapi.dto.cart.ShoppingCartDto;
import ru.yandex.practicum.commerce.interactionapi.dto.common.AddressDto;
import ru.yandex.practicum.commerce.interactionapi.dto.common.BookedProductsDto;
import ru.yandex.practicum.commerce.interactionapi.dto.warehouse.*;
import ru.yandex.practicum.commerce.interactionapi.operation.WarehouseOperation;
import ru.yandex.practicum.commerce.warehouse.service.WarehouseService;

import java.util.Map;
import java.util.UUID;

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

    @Override
    public void shippedToDelivery(ShippedToDeliveryRequest request) {
        log.info("--> POST request to hand over the order to delivery: {}", request);
        warehouseService.shippedToDelivery(request);
        log.info("<-- POST response");
    }

    @Override
    public void acceptReturn(Map<UUID, Long> products) {
        log.info("--> POST request to accept returned products back to the warehouse: {}", products);
        warehouseService.acceptReturn(products);
        log.info("<-- POST response");
    }

    @Override
    public BookedProductsDto assemblyProductsForOrder(AssemblyProductsForOrderRequest request) {
        log.info("--> POST request to assemble products for the order in preparation for shipment: {}", request);
        BookedProductsDto bookedProductsDto = warehouseService.assemblyProductsForOrder(request);
        log.info("<-- POST response: {}", bookedProductsDto);
        return bookedProductsDto;
    }

}