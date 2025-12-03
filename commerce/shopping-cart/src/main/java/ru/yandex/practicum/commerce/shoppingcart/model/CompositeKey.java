package ru.yandex.practicum.commerce.shoppingcart.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CompositeKey implements Serializable {

    UUID shoppingCartId;

    UUID productId;
}
