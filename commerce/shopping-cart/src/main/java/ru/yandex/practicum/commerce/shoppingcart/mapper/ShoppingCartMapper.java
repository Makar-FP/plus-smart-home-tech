package ru.yandex.practicum.commerce.shoppingcart.mapper;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.commerce.interactionapi.dto.ShoppingCartDto;
import ru.yandex.practicum.commerce.shoppingcart.model.ShoppingCart;

import java.util.*;
import java.util.stream.Collectors;

@Component
public class ShoppingCartMapper {

    public ShoppingCartDto toShoppingCartDto(List<ShoppingCart> items) {
        if (items == null || items.isEmpty()) {
            return new ShoppingCartDto(null, null, Map.of());
        }

        UUID cartId = items.getFirst().getShoppingCartId();
        String username = items.getFirst().getUsername();

        Map<UUID, Long> products = items.stream()
                .collect(Collectors.toMap(
                        ShoppingCart::getProductId,
                        sc -> sc.getQuantity() == null ? 0L : sc.getQuantity(),
                        Long::sum
                ));

        return new ShoppingCartDto(cartId, username, products);
    }
}
