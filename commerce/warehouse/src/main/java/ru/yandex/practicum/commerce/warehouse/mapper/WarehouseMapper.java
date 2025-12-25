package ru.yandex.practicum.commerce.warehouse.mapper;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.commerce.interactionapi.dto.warehouse.AssemblyProductsForOrderRequest;
import ru.yandex.practicum.commerce.interactionapi.dto.warehouse.DimensionDto;
import ru.yandex.practicum.commerce.interactionapi.dto.warehouse.NewProductInWarehouseRequest;
import ru.yandex.practicum.commerce.interactionapi.dto.warehouse.WarehouseDto;
import ru.yandex.practicum.commerce.warehouse.model.BookingProduct;
import ru.yandex.practicum.commerce.warehouse.model.OrderBooking;
import ru.yandex.practicum.commerce.warehouse.model.WarehouseProduct;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

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

    public OrderBooking toOrderBooking(AssemblyProductsForOrderRequest request) {
        if (request == null) throw new IllegalArgumentException("request must not be null");
        if (request.getOrderId() == null) throw new IllegalArgumentException("orderId must not be null");
        if (request.getProducts() == null || request.getProducts().isEmpty()) {
            throw new IllegalArgumentException("products must not be empty");
        }

        OrderBooking orderBooking = new OrderBooking();
        orderBooking.setOrderId(request.getOrderId());

        List<BookingProduct> products = request.getProducts().entrySet().stream()
                .map(e -> {
                    UUID productId = e.getKey();
                    Long qty = e.getValue();

                    if (productId == null) throw new IllegalArgumentException("productId must not be null");
                    if (qty == null || qty <= 0) {
                        throw new IllegalArgumentException("quantity must be > 0 for productId=" + productId);
                    }

                    BookingProduct bp = new BookingProduct();
                    bp.setId(UUID.randomUUID());
                    bp.setProductId(productId);
                    bp.setQuantity(qty);

                    return bp;
                })
                .toList();

        orderBooking.setProducts(products);
        return orderBooking;
    }


    private static BigDecimal nvl(BigDecimal v) {
        return v == null ? BigDecimal.ZERO : v;
    }
}
