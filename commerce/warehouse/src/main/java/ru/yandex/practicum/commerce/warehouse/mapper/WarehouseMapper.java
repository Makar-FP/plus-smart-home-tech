package ru.yandex.practicum.commerce.warehouse.mapper;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.commerce.interactionapi.dto.DimensionDto;
import ru.yandex.practicum.commerce.interactionapi.dto.NewProductInWarehouseRequest;
import ru.yandex.practicum.commerce.interactionapi.dto.WarehouseDto;
import ru.yandex.practicum.commerce.warehouse.model.WarehouseProduct;

import java.math.BigDecimal;

@Component
public class WarehouseMapper {

    public WarehouseProduct toProduct(NewProductInWarehouseRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("request is null");
        }

        DimensionDto d = request.getDimension();

        WarehouseProduct product = new WarehouseProduct();
        product.setId(request.getProductId());
        product.setFragile(request.isFragile());
        product.setWeight(nvl(request.getWeight()));
        
        product.setWidth(d == null ? BigDecimal.ZERO : nvl(d.getWidth()));
        product.setHeight(d == null ? BigDecimal.ZERO : nvl(d.getHeight()));
        product.setDepth(d == null ? BigDecimal.ZERO : nvl(d.getDepth()));

        return product;
    }

    public WarehouseDto toWarehouseDto(WarehouseProduct product) {
        DimensionDto dimension = new DimensionDto(
                nvl(product.getDepth()),
                nvl(product.getHeight()),
                nvl(product.getWidth())
        );

        return new WarehouseDto(
                product.getId(),
                nvl(product.getWeight()),
                dimension,
                product.isFragile()
        );
    }

    private static BigDecimal nvl(BigDecimal v) {
        return v == null ? BigDecimal.ZERO : v;
    }
}
