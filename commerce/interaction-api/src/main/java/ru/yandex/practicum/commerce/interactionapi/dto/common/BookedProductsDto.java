package ru.yandex.practicum.commerce.interactionapi.dto.common;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
public class BookedProductsDto {

    private BigDecimal deliveryWeight;

    private BigDecimal deliveryVolume;

    private boolean fragile;
}
