package ru.yandex.practicum.commerce.payment.service;

import ru.yandex.practicum.commerce.interactionapi.dto.order.OrderDto;
import ru.yandex.practicum.commerce.interactionapi.dto.payment.PaymentDto;

import java.math.BigDecimal;
import java.util.UUID;

public interface PaymentService {

    PaymentDto payment(OrderDto order);

    BigDecimal getTotalCost(OrderDto order);

    void paymentSuccess(UUID paymentId);

    BigDecimal productCost(OrderDto order);

    void paymentFailed(UUID paymentId);

}
