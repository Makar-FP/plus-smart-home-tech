package ru.yandex.practicum.commerce.warehouse.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

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
    private BigDecimal weight = BigDecimal.ZERO;

    @Column(nullable = false)
    private BigDecimal depth = BigDecimal.ZERO;

    @Column(nullable = false)
    private BigDecimal height = BigDecimal.ZERO;

    @Column(nullable = false)
    private BigDecimal width = BigDecimal.ZERO;

    @Column(nullable = false)
    private boolean fragile;

    @Column(nullable = false)
    private Long quantity = 0L;
}
