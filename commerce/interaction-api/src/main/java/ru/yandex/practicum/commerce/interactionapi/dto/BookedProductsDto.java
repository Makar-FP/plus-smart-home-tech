package ru.yandex.practicum.commerce.interactionapi.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
public class BookedProductsDto {

    private Double deliveryWeight;

    private Double deliveryVolume;

    private boolean fragile;
}
