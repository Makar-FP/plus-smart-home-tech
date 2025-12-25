package ru.yandex.practicum.commerce.order.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.commerce.interactionapi.dto.order.CreateNewOrderRequest;
import ru.yandex.practicum.commerce.interactionapi.dto.order.OrderDto;
import ru.yandex.practicum.commerce.interactionapi.dto.order.OrderState;
import ru.yandex.practicum.commerce.interactionapi.dto.order.ProductReturnRequest;
import ru.yandex.practicum.commerce.interactionapi.operation.OrderOperation;
import ru.yandex.practicum.commerce.order.service.OrderService;

import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/api/v1/order")
public class OrderController implements OrderOperation {
    private final OrderService orderService;

    @Override
    @GetMapping
    public List<OrderDto> getClientOrders(String username) {
        log.info("--> GET request with username={}", username);
        List<OrderDto> orders = orderService.getClientOrders(username);
        log.info("<-- GET response orders={}", orders);
        return orders;
    }

    @Override
    @PutMapping
    public OrderDto createNewOrder(CreateNewOrderRequest request) {
        log.info("--> PUT request with CreateNewOrderRequest={}", request);
        OrderDto order = orderService.createNewOrder(request);
        log.info("<-- PUT response order={}", order);
        return order;
    }

    @Override
    @PostMapping("/return")
    public OrderDto productReturn(ProductReturnRequest request) {
        log.info("--> POST request for order return: {}", request);
        OrderDto order = orderService.productReturn(request);
        log.info("<-- POST response order={}", order);
        return order;
    }

    @Override
    @PostMapping("/payment")
    public OrderDto payment(UUID orderId) {
        log.info("--> POST request to pay for order: {}", orderId);
        OrderDto order = orderService.updateOrderStatus(orderId, OrderState.PAID);
        log.info("<-- POST response order={}", order);
        return order;
    }

    @Override
    @PostMapping("/payment/failed")
    public OrderDto paymentFailed(UUID orderId) {
        log.info("--> POST request for payment failure: {}", orderId);
        OrderDto order = orderService.updateOrderStatus(orderId, OrderState.PAYMENT_FAILED);
        log.info("<-- POST response order={}", order);
        return order;
    }

    @Override
    @PostMapping("/delivery")
    public OrderDto delivery(UUID orderId) {
        log.info("--> POST request for order delivery: {}", orderId);
        OrderDto order = orderService.updateOrderStatus(orderId, OrderState.DELIVERED);
        log.info("<-- POST response order={}", order);
        return order;
    }

    @Override
    @PostMapping("/delivery/failed")
    public OrderDto deliveryFailed(UUID orderId) {
        log.info("--> POST request for delivery failure: {}", orderId);
        OrderDto order = orderService.updateOrderStatus(orderId, OrderState.DELIVERY_FAILED);
        log.info("<-- POST response order={}", order);
        return order;
    }

    @Override
    @PostMapping("/completed")
    public OrderDto complete(UUID orderId) {
        log.info("--> POST request to complete order: {}", orderId);
        OrderDto order = orderService.updateOrderStatus(orderId, OrderState.COMPLETED);
        log.info("<-- POST response order={}", order);
        return order;
    }

    @Override
    @PostMapping("/calculate/total")
    public OrderDto calculateTotalCost(UUID orderId) {
        log.info("--> POST request to calculate total order cost: {}", orderId);
        OrderDto order = orderService.calculateTotalCost(orderId);
        log.info("<-- POST response order={}", order);
        return order;
    }

    @Override
    @PostMapping("/calculate/delivery")
    public OrderDto calculateDeliveryCost(UUID orderId) {
        log.info("--> POST request to calculate delivery cost: {}", orderId);
        OrderDto order = orderService.calculateDeliveryCost(orderId);
        log.info("<-- POST response order={}", order);
        return order;
    }

    @Override
    @PostMapping("/assembly")
    public OrderDto assembly(UUID orderId) {
        log.info("--> POST request for order assembly: {}", orderId);
        OrderDto order = orderService.updateOrderStatus(orderId, OrderState.ASSEMBLED);
        log.info("<-- POST response order={}", order);
        return order;
    }

    @Override
    @PostMapping("/assembly/failed")
    public OrderDto assemblyFailed(UUID orderId) {
        log.info("--> POST request for assembly failure: {}", orderId);
        OrderDto order = orderService.updateOrderStatus(orderId, OrderState.ASSEMBLY_FAILED);
        log.info("<-- POST response order={}", order);
        return order;
    }
}
