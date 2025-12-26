package ru.yandex.practicum.commerce.order.service;

import ru.yandex.practicum.commerce.interactionapi.dto.order.CreateNewOrderRequest;
import ru.yandex.practicum.commerce.interactionapi.dto.order.OrderDto;
import ru.yandex.practicum.commerce.interactionapi.dto.order.OrderState;
import ru.yandex.practicum.commerce.interactionapi.dto.order.ProductReturnRequest;

import java.util.List;
import java.util.UUID;

public interface OrderService {

    List<OrderDto> getClientOrders(String username);

    OrderDto createNewOrder(CreateNewOrderRequest request);

    OrderDto productReturn(ProductReturnRequest request);

    OrderDto updateOrderStatus(UUID orderId, OrderState state);

    OrderDto calculateTotalCost(UUID orderId);

    OrderDto calculateDeliveryCost(UUID orderId);

}
