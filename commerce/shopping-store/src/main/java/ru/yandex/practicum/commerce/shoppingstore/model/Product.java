package ru.yandex.practicum.commerce.shoppingstore.model;

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
public class Product {
    @Id
    @Column(nullable = false, name = "id")
    private UUID id;

    @Column(nullable = false, name = "product_name")
    private String productName;

    @Column(nullable = false)
    private String description;

    @Column(nullable = false, name = "image_src")
    private String imageSrc;

    @Column(nullable = false, name = "quantity_state")
    private String quantityState;

    @Column(nullable = false, name = "product_state")
    private String productState;

    @Column(nullable = false, name = "product_category")
    private String productCategory;

    @Column(nullable = false)
    private BigDecimal price;
}
