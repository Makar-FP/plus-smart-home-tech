package ru.yandex.practicum.commerce.payment.mapper;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.commerce.interactionapi.dto.order.OrderDto;
import ru.yandex.practicum.commerce.interactionapi.dto.payment.PaymentDto;
import ru.yandex.practicum.commerce.interactionapi.dto.payment.PaymentStatus;
import ru.yandex.practicum.commerce.payment.model.Payment;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;
import java.util.UUID;

@Component
public class PaymentMapper {
    private static final BigDecimal FEE_RATE = new BigDecimal("0.10");

    private static final int MONEY_SCALE = 2;
    private static final RoundingMode MONEY_ROUNDING = RoundingMode.HALF_UP;

    public Payment toPayment(OrderDto order) {
        Objects.requireNonNull(order, "order must not be null");
        Objects.requireNonNull(order.getOrderId(), "orderId must not be null");

        BigDecimal product = nz(order.getProductPrice());
        BigDecimal delivery = nz(order.getDeliveryPrice());
        BigDecimal total = order.getTotalPrice() != null ? order.getTotalPrice() : product.add(delivery);

        Payment payment = new Payment();
        payment.setPaymentId(UUID.randomUUID());
        payment.setOrderId(order.getOrderId());
        payment.setProductPrice(money(product));
        payment.setDeliverPrice(money(delivery));
        payment.setTotalPrice(money(total));
        payment.setPaymentStatus(PaymentStatus.PENDING);

        return payment;
    }

    public PaymentDto toPaymentDto(Payment payment) {
        Objects.requireNonNull(payment, "payment must not be null");
        Objects.requireNonNull(payment.getPaymentId(), "paymentId must not be null");

        BigDecimal product = nz(payment.getProductPrice());
        BigDecimal delivery = nz(payment.getDeliverPrice());
        BigDecimal total = payment.getTotalPrice() != null ? payment.getTotalPrice() : product.add(delivery);

        BigDecimal fee = product.multiply(FEE_RATE);

        PaymentDto dto = new PaymentDto();
        dto.setPaymentId(payment.getPaymentId());
        dto.setTotalPayment(money(total));
        dto.setDeliveryTotal(money(delivery));
        dto.setFeeTotal(money(fee));

        return dto;
    }

    private static BigDecimal nz(BigDecimal v) {
        return v != null ? v : BigDecimal.ZERO;
    }

    private static BigDecimal money(BigDecimal v) {
        return nz(v).setScale(MONEY_SCALE, MONEY_ROUNDING);
    }
}
