package ru.yandex.practicum.commerce.delivery.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.commerce.delivery.service.DeliveryService;
import ru.yandex.practicum.commerce.interactionapi.dto.delivery.DeliveryDto;
import ru.yandex.practicum.commerce.interactionapi.dto.order.OrderDto;
import ru.yandex.practicum.commerce.interactionapi.operation.DeliveryOperation;

import java.math.BigDecimal;
import java.util.UUID;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/api/v1/delivery")
public class DeliveryController implements DeliveryOperation {

    private final DeliveryService deliveryService;

    @Override
    @PutMapping
    public DeliveryDto planDelivery(DeliveryDto deliveryDto) {
        log.info("PUT /delivery — planning delivery: {}", deliveryDto);
        DeliveryDto result = deliveryService.planDelivery(deliveryDto);
        log.info("PUT /delivery — delivery planned successfully: {}", result);
        return result;
    }

    @Override
    @PostMapping("/successful")
    public void deliverySuccessful(UUID orderId) {
        log.info("POST /delivery/successful — delivery completed successfully, orderId={}", orderId);
        deliveryService.deliverySuccessful(orderId);
    }

    @Override
    @PostMapping("/picked")
    public void deliveryPicked(UUID orderId) {
        log.info("POST /delivery/picked — order picked up for delivery, orderId={}", orderId);
        deliveryService.deliveryPicked(orderId);
    }

    @Override
    @PostMapping("/failed")
    public void deliveryFailed(UUID orderId) {
        log.info("POST /delivery/failed — delivery failed, orderId={}", orderId);
        deliveryService.deliveryFailed(orderId);
    }

    @Override
    @PostMapping("/cost")
    public BigDecimal deliveryCost(OrderDto order) {
        log.info("POST /delivery/cost — calculating delivery cost for order: {}", order);
        BigDecimal result = deliveryService.deliveryCost(order);
        log.info("POST /delivery/cost — delivery cost calculated: {}", result);
        return result;
    }
}
