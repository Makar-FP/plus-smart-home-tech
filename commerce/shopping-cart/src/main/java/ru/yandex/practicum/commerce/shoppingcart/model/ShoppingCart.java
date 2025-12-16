package ru.yandex.practicum.commerce.shoppingcart.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "shopping_carts")
@IdClass(CompositeKey.class)
public class ShoppingCart {

    @Id
    @Column(nullable = false, name = "shopping_cart_id")
    private UUID shoppingCartId;

    @Id
    @Column(nullable = false, name = "product_id")
    private UUID productId;

    @Column(nullable = false)
    private String username;

    @Column(nullable = false)
    private Long quantity;
}

