package ru.yandex.practicum.commerce.warehouse.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "orders_booking")
public class OrderBooking {

    @Id
    @Column(nullable = false, name = "id")
    private UUID orderId;

    @Column(name = "delivery_id")
    private UUID deliveryId;

    @OneToMany(cascade = CascadeType.ALL)
    @JoinColumn(name = "order_id")
    private List<BookingProduct> products = new ArrayList<>();

}
