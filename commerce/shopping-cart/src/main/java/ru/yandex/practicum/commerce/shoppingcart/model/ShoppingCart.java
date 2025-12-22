package ru.yandex.practicum.commerce.shoppingcart.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "shopping_carts")
@IdClass(CompositeKey.class)
public class ShoppingCart {

    @Id
    @Column(nullable = false, name = "id")
    private UUID id;

    @Id
    @Column(nullable = false, name = "product_id")
    private UUID productId;

    @Column(nullable = false)
    private String username;

    @Column(nullable = false)
    private Long quantity;
}

