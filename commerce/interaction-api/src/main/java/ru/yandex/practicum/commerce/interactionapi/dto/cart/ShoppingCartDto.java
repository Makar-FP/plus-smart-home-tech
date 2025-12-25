package ru.yandex.practicum.commerce.interactionapi.dto.cart;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;
import java.util.UUID;

@Data
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
public class ShoppingCartDto {

    private UUID shoppingCartId;

    private String username;

    private Map<UUID, Long> products;

}
