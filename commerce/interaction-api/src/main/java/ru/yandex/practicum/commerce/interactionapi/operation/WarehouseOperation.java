package ru.yandex.practicum.commerce.interactionapi.operation;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import ru.yandex.practicum.commerce.interactionapi.dto.*;

public interface WarehouseOperation {

    @PutMapping
    WarehouseDto newProductInWarehouse(@RequestBody NewProductInWarehouseRequest request);

    @PostMapping("/add")
    WarehouseDto addProductToWarehouse(@RequestBody AddProductToWarehouseRequest request);

    @GetMapping("/address")
    AddressDto getWarehouseAddress();

    @PostMapping("/check")
    BookedProductsDto checkProductQuantityEnoughForShoppingCart(@RequestBody ShoppingCartDto request);
}
