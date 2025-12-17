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
            return ShoppingCartDto.builder()
                    .shoppingCartId(null)
                    .username(null)
                    .products(Map.of())
                    .build();
        }

        UUID cartId = items.get(0).getShoppingCartId();
        String username = items.get(0).getUsername();

        Map<UUID, Long> products = items.stream()
                .collect(Collectors.toMap(
                        ShoppingCart::getProductId,
                        ShoppingCart::getQuantity,
                        Long::sum
                ));

        return ShoppingCartDto.builder()
                .shoppingCartId(cartId)
                .username(username)
                .products(products)
                .build();
    }
}

