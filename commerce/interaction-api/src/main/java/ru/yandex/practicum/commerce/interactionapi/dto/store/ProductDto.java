package ru.yandex.practicum.commerce.interactionapi.dto.store;

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
public class ProductDto {

    private UUID productId;

    private String productName;

    private String description;

    private String imageSrc;

    private String quantityState;

    private String productState;

    private String productCategory;

    private BigDecimal price;
}
