package ru.yandex.practicum.commerce.delivery.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.yandex.practicum.commerce.interactionapi.dto.delivery.DeliveryState;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Builder
@Table(name = "deliveries")
public class Delivery {

    @Id
    @Column(nullable = false, name = "id")
    private UUID deliveryId;

    @Column(name = "delivery_weight")
    private Double deliveryWeight = 0D;

    @Column(name = "delivery_volume")
    private Double deliveryVolume = 0D;

    @Column(nullable = false)
    private boolean fragile = false;

    @Column(name = "delivery_status")
    private DeliveryState deliveryState = DeliveryState.CREATED;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "address_from_id", referencedColumnName = "id")
    private DeliveryAddress deliveryFromAddress;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "address_to_id", referencedColumnName = "id")
    private DeliveryAddress deliveryToAddress;

    @Column(name = "order_id")
    private UUID orderId;
}
