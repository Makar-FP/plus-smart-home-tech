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
    @Column(nullable = false, name = "id")
    UUID shoppingCartId;

    @Id
    @Column(nullable = false, name = "product_id")
    UUID productId;

    @Column(nullable = false)
    String username;

    @Column(nullable = false)
    Long quantity;
}
