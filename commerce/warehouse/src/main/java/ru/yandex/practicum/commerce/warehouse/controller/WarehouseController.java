package ru.yandex.practicum.commerce.warehouse.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.commerce.interactionapi.dto.AddProductToWarehouseRequest;
import ru.yandex.practicum.commerce.interactionapi.dto.AddressDto;
import ru.yandex.practicum.commerce.interactionapi.dto.BookedProductsDto;
import ru.yandex.practicum.commerce.interactionapi.dto.NewProductInWarehouseRequest;
import ru.yandex.practicum.commerce.interactionapi.dto.ShoppingCartDto;
import ru.yandex.practicum.commerce.interactionapi.dto.WarehouseDto;
import ru.yandex.practicum.commerce.interactionapi.operation.WarehouseOperation;
import ru.yandex.practicum.commerce.warehouse.service.WarehouseService;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/api/v1/warehouse")
public class WarehouseController implements WarehouseOperation {

    private final WarehouseService warehouseService;

    @Override
    public WarehouseDto newProductInWarehouse(NewProductInWarehouseRequest request) {
        log.info("PUT request to add new product to warehouse: {}", request);
        WarehouseDto newProduct = warehouseService.newProductInWarehouse(request);
        log.info("PUT response with created warehouse product: {}", newProduct);
        return newProduct;
    }

    @Override
    public WarehouseDto addProductToWarehouse(AddProductToWarehouseRequest request) {
        log.info("POST request to update product quantity in warehouse: {}", request);
        WarehouseDto updatedProduct = warehouseService.addProductToWarehouse(request);
        log.info("POST response with updated warehouse product: {}", updatedProduct);
        return updatedProduct;
    }

    @Override
    public AddressDto getWarehouseAddress() {
        log.info("GET request to retrieve warehouse address");
        AddressDto address = warehouseService.getWarehouseAddress();
        log.info("GET response with warehouse address: {}", address);
        return address;
    }

    @Override
    public BookedProductsDto checkProductQuantityEnoughForShoppingCart(ShoppingCartDto request) {
        log.info("POST request to check product quantities for shopping cart: {}", request);
        BookedProductsDto result = warehouseService.checkProductQuantityEnoughForShoppingCart(request);
        log.info("POST response with booked products info: {}", result);
        return result;
    }
}

