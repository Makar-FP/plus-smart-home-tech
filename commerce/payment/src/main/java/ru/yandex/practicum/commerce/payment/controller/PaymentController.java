package ru.yandex.practicum.commerce.payment.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.commerce.interactionapi.dto.order.OrderDto;
import ru.yandex.practicum.commerce.interactionapi.dto.payment.PaymentDto;
import ru.yandex.practicum.commerce.interactionapi.operation.PaymentOperation;
import ru.yandex.practicum.commerce.payment.service.PaymentService;

import java.math.BigDecimal;
import java.util.UUID;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/api/v1/payment")
public class PaymentController implements PaymentOperation {

    private final PaymentService paymentService;

    @Override
    @PostMapping
    public PaymentDto payment(OrderDto order) {
        log.info("--> POST request to create payment: {}", order);
        PaymentDto payment = paymentService.payment(order);
        log.info("<-- POST response payment={}", payment);
        return payment;
    }

    @Override
    @PostMapping("/totalCost")
    public BigDecimal getTotalCost(OrderDto order) {
        log.info("--> POST request to calculate order total cost: {}", order);
        BigDecimal result = paymentService.getTotalCost(order);
        log.info("<-- POST response result={}", result);
        return result;
    }

    @Override
    @PostMapping("/refund")
    public void paymentSuccess(UUID paymentId) {
        log.info("--> POST request to emulate successful payment in payment gateway: {}", paymentId);
        paymentService.paymentSuccess(paymentId);
        log.info("<-- POST response: payment marked as successful");
    }

    @Override
    @PostMapping("/productCost")
    public BigDecimal productCost(OrderDto order) {
        log.info("--> POST request to calculate products cost in order: {}", order);
        BigDecimal result = paymentService.productCost(order);
        log.info("<-- POST response result={}", result);
        return result;
    }

    @Override
    @PostMapping("/failed")
    public void paymentFailed(UUID paymentId) {
        log.info("--> POST request to emulate failed payment in payment gateway: {}", paymentId);
        paymentService.paymentFailed(paymentId);
        log.info("<-- POST response: payment marked as failed");
    }
}

