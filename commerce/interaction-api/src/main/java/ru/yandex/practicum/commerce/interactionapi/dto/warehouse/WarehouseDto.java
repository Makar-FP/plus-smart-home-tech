package ru.yandex.practicum.commerce.interactionapi.dto.warehouse;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
public class WarehouseDto {

    private UUID productId;

    private BigDecimal weight;

    private DimensionDto dimension;

    private boolean fragile;

}
