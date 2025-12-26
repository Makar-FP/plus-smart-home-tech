package ru.yandex.practicum.commerce.order.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.yandex.practicum.commerce.interactionapi.dto.order.OrderState;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Builder
@Table(name = "orders",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"username"})
        }
)
public class Order {
    @Id
    @Column(nullable = false, name = "id")
    private UUID orderId;

    @Column(nullable = false)
    private String username;

    @Column(name = "cart_id")
    private UUID shoppingCartId;


    @Column(name = "payment_id")
    private UUID paymentId;

    @Column(name = "delivery_id")
    private UUID deliveryId;

    @Column(nullable = false)
    private OrderState state = OrderState.NEW;

    @Column(name = "delivery_weight")
    private BigDecimal deliveryWeight = BigDecimal.valueOf(0D);

    @Column(name = "delivery_volume")
    private BigDecimal deliveryVolume = BigDecimal.valueOf(0D);

    @Column(nullable = false)
    private boolean fragile = false;

    @Column(name = "total_price")
    private BigDecimal totalPrice = BigDecimal.ZERO;

    @Column(name = "deliver_price")
    private BigDecimal deliverPrice = BigDecimal.ZERO;

    @Column(name = "product_price")
    private BigDecimal productPrice = BigDecimal.ZERO;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "address_id", referencedColumnName = "id")
    private OrderAddress orderAddress;

    @OneToMany(cascade = CascadeType.ALL)
    @JoinColumn(name = "order_id")
    private List<OrderProduct> products = new ArrayList<>();
}

