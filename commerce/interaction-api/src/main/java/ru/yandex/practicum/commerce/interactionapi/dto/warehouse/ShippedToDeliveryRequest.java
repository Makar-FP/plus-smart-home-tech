package ru.yandex.practicum.commerce.interactionapi.dto.warehouse;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
public class ShippedToDeliveryRequest {

    private UUID deliveryId;

    private UUID orderId;

}
