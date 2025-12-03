package ru.yandex.practicum.commerce.interactionapi.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
public class DimensionDto {

    private Double depth;

    private Double height;

    private Double width;

}
