package ru.yandex.practicum.commerce.interactionapi.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.commerce.interactionapi.dto.cart.ShoppingCartDto;
import ru.yandex.practicum.commerce.interactionapi.dto.common.AddressDto;
import ru.yandex.practicum.commerce.interactionapi.dto.common.BookedProductsDto;
import ru.yandex.practicum.commerce.interactionapi.dto.warehouse.*;

@FeignClient(name = "warehouse")
public interface WarehouseClient {

    @GetMapping("/api/v1/warehouse/address")
    AddressDto getWarehouseAddress();

    @PostMapping("/api/v1/warehouse/shipped")
    void shippedToDelivery(@RequestBody ShippedToDeliveryRequest request);

    @PostMapping("/api/v1/warehouse/assembly")
    BookedProductsDto assemblyProductsForOrder(@RequestBody AssemblyProductsForOrderRequest request);

    @PostMapping("/api/v1/warehouse/check")
    BookedProductsDto checkProductQuantityEnoughForShoppingCart(@RequestBody ShoppingCartDto request);
}
