package ru.yandex.practicum.commerce.warehouse.mapper;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.commerce.interactionapi.dto.AddressDto;
import ru.yandex.practicum.commerce.interactionapi.dto.DimensionDto;
import ru.yandex.practicum.commerce.interactionapi.dto.NewProductInWarehouseRequest;
import ru.yandex.practicum.commerce.interactionapi.dto.WarehouseDto;
import ru.yandex.practicum.commerce.warehouse.model.WarehouseProduct;

import java.math.BigDecimal;

@Component
public class WarehouseMapper {

    private static final BigDecimal ZERO = BigDecimal.ZERO;

    public WarehouseProduct toProduct(NewProductInWarehouseRequest request) {
        WarehouseProduct product = new WarehouseProduct();

        product.setId(request.getProductId());
        product.setFragile(request.isFragile());
        product.setWeight(nvl(request.getWeight()));

        DimensionDto dim = request.getDimension();
        product.setWidth(nvl(dim == null ? null : dim.getWidth()));
        product.setHeight(nvl(dim == null ? null : dim.getHeight()));
        product.setDepth(nvl(dim == null ? null : dim.getDepth()));

        return product;
    }

    public WarehouseDto toWarehouseDto(WarehouseProduct product) {
        DimensionDto dimension = new DimensionDto();
        dimension.setDepth(nvl(product.getDepth()));
        dimension.setHeight(nvl(product.getHeight()));
        dimension.setWidth(nvl(product.getWidth()));

        return new WarehouseDto(
                product.getId(),
                nvl(product.getWeight()),
                dimension,
                product.isFragile()
        );
    }

    public AddressDto toAddressDto(String rawAddress) {
        if (rawAddress == null) {
            return null;
        }

        String trimmed = rawAddress.trim();
        if (trimmed.isEmpty()) {
            return new AddressDto("", "", "", "", "");
        }

        String[] parts = trimmed.split("\\s*,\\s*");
        if (parts.length == 5) {
            return new AddressDto(parts[0], parts[1], parts[2], parts[3], parts[4]);
        }

        return new AddressDto(trimmed, trimmed, trimmed, trimmed, trimmed);
    }

    private static BigDecimal nvl(BigDecimal v) {
        return v == null ? ZERO : v;
    }
}
