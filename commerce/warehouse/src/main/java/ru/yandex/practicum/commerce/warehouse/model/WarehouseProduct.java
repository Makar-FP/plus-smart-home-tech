package ru.yandex.practicum.commerce.warehouse.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.persistence.*;

import java.math.BigDecimal;
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
    private BigDecimal weight;

    @Column(nullable = false)
    private BigDecimal depth;

    @Column(nullable = false)
    private BigDecimal height;

    @Column(nullable = false)
    private BigDecimal width;

    @Column(nullable = false)
    private boolean fragile;

    @Column(nullable = false)
    private Long quantity = 0L;

}