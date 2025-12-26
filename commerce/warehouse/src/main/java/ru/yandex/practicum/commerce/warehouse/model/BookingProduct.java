package ru.yandex.practicum.commerce.warehouse.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "booking_products")
public class BookingProduct {

    @Id
    @Column(nullable = false, name = "id")
    private UUID Id;

    @Column(nullable = false, name = "product_id")
    private UUID productId;

    @Column(name = "quantity")
    private Long quantity;

}
