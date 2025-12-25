package ru.yandex.practicum.commerce.interactionapi.dto.order;

import lombok.*;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

@Data
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
public class OrderDto {

    @NonNull
    private UUID orderId;

    private UUID shoppingCartId;

    @NonNull
    private Map<UUID, Long> products;

    private UUID paymentId;

    private UUID deliveryId;

    private OrderState state;

    private BigDecimal deliveryWeight;

    private BigDecimal deliveryVolume;

    private boolean fragile;

    private BigDecimal totalPrice;

    private BigDecimal deliveryPrice;

    private BigDecimal productPrice;

}
