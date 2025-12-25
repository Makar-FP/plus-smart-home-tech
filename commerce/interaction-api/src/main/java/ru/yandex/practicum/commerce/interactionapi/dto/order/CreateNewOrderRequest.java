package ru.yandex.practicum.commerce.interactionapi.dto.order;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.yandex.practicum.commerce.interactionapi.dto.common.AddressDto;
import ru.yandex.practicum.commerce.interactionapi.dto.cart.ShoppingCartDto;

@Data
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
public class CreateNewOrderRequest {

    private AddressDto deliveryAddress;

    private ShoppingCartDto shoppingCart;
}
