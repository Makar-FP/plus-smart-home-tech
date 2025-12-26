package ru.yandex.practicum.commerce.interactionapi.dto.order;

import lombok.*;

import java.util.Map;
import java.util.UUID;

@Data
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
public class ProductReturnRequest {

    private UUID orderId;

    @NonNull
    private Map<UUID, Long> products;

}
