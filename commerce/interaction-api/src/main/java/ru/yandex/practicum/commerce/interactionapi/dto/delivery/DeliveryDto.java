package ru.yandex.practicum.commerce.interactionapi.dto.delivery;

import lombok.*;
import ru.yandex.practicum.commerce.interactionapi.dto.common.AddressDto;

import java.util.UUID;

@Data
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
public class DeliveryDto {

    @NonNull
    private UUID deliveryId;

    private DeliveryState deliveryState;

    private AddressDto fromAddress;

    private UUID orderId;

    private AddressDto toAddress;
}
