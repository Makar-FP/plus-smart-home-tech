package ru.yandex.practicum.commerce.payment.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.yandex.practicum.commerce.interactionapi.dto.payment.PaymentStatus;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Builder
@Table(name = "payments")
public class Payment {
    @Id
    @Column(nullable = false, name = "id")
    private UUID paymentId;

    @Column(name = "order_id")
    private UUID orderId;

    @Column(name = "total_price")
    private BigDecimal totalPrice;

    @Column(name = "delivery_price")
    private BigDecimal deliverPrice;

    @Column(name = "product_price")
    private BigDecimal productPrice;

    @Column(name = "payment_status")
    private PaymentStatus paymentStatus = PaymentStatus.PENDING;

}
