package ru.yandex.practicum.commerce.warehouse.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.persistence.*;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "products")
public class WarehouseProduct {

    @Id
    @Column(nullable = false, name = "id")
    private UUID id;

    @Column(nullable = false)
    private Double weight;

    @Column(nullable = false)
    private Double depth;

    @Column(nullable = false)
    private Double height;

    @Column(nullable = false)
    private Double width;

    @Column(nullable = false)
    private boolean fragile;

    @Column(nullable = false)
    private Long quantity = 0L;

}