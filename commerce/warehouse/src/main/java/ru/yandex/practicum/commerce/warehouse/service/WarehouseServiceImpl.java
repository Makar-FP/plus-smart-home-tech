package ru.yandex.practicum.commerce.warehouse.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.commerce.interactionapi.dto.cart.ShoppingCartDto;
import ru.yandex.practicum.commerce.interactionapi.dto.common.AddressDto;
import ru.yandex.practicum.commerce.interactionapi.dto.common.BookedProductsDto;
import ru.yandex.practicum.commerce.interactionapi.dto.warehouse.*;
import ru.yandex.practicum.commerce.interactionapi.exception.NoOrderFoundException;
import ru.yandex.practicum.commerce.interactionapi.exception.NoSpecifiedProductInWarehouseException;
import ru.yandex.practicum.commerce.interactionapi.exception.ProductInShoppingCartLowQuantityInWarehouse;
import ru.yandex.practicum.commerce.warehouse.mapper.WarehouseMapper;
import ru.yandex.practicum.commerce.warehouse.model.OrderBooking;
import ru.yandex.practicum.commerce.warehouse.model.WarehouseProduct;
import ru.yandex.practicum.commerce.warehouse.repo.OrderBookingRepository;
import ru.yandex.practicum.commerce.warehouse.repo.WarehouseRepository;

import java.math.BigDecimal;
import java.security.SecureRandom;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class WarehouseServiceImpl implements WarehouseService {

    private static final AddressDto[] ADDRESSES = {
            new AddressDto("Russia", "Moscow", "Tverskaya", "1", "1"),
            new AddressDto("Russia", "Saint Petersburg", "Nevsky", "10", "25")
    };
    private static final AddressDto CURRENT_ADDRESS =
            ADDRESSES[new SecureRandom().nextInt(ADDRESSES.length)];

    private final WarehouseRepository warehouseRepository;
    private final OrderBookingRepository orderBookingRepository;
    private final WarehouseMapper mapper;

    @Override
    @Transactional
    public WarehouseDto newProductInWarehouse(NewProductInWarehouseRequest request) {
        WarehouseProduct product = mapper.toProduct(request);
        WarehouseProduct saved = warehouseRepository.save(product);
        return mapper.toWarehouseDto(saved);
    }

    @Override
    @Transactional
    public WarehouseDto addProductToWarehouse(AddProductToWarehouseRequest request) {
        WarehouseProduct product = warehouseRepository.findById(request.getProductId())
                .orElseThrow(() -> new NoSpecifiedProductInWarehouseException(request.getProductId()));

        long addQty = nvl(request.getQuantity());
        long current = nvl(product.getQuantity());

        product.setQuantity(current + addQty);

        WarehouseProduct saved = warehouseRepository.save(product);
        return mapper.toWarehouseDto(saved);
    }

    @Override
    public AddressDto getWarehouseAddress() {
        return CURRENT_ADDRESS;
    }

    @Override
    public BookedProductsDto checkProductQuantityEnoughForShoppingCart(ShoppingCartDto request) {
        if (request == null || request.getProducts() == null || request.getProducts().isEmpty()) {
            return new BookedProductsDto(BigDecimal.ZERO, BigDecimal.ZERO, false);
        }

        BigDecimal totalWeight = BigDecimal.ZERO;
        BigDecimal totalVolume = BigDecimal.ZERO;
        boolean fragile = false;

        for (Map.Entry<UUID, Long> entry : request.getProducts().entrySet()) {
            UUID productId = entry.getKey();
            long cartQty = nvl(entry.getValue());

            WarehouseProduct product = warehouseRepository.findById(productId)
                    .orElseThrow(() -> new NoSpecifiedProductInWarehouseException(productId));

            long warehouseQty = nvl(product.getQuantity());
            if (warehouseQty < cartQty) {
                throw new ProductInShoppingCartLowQuantityInWarehouse(productId);
            }

            BigDecimal qty = BigDecimal.valueOf(cartQty);

            totalWeight = totalWeight.add(nvl(product.getWeight()).multiply(qty));

            BigDecimal oneItemVolume = nvl(product.getDepth())
                    .multiply(nvl(product.getWidth()))
                    .multiply(nvl(product.getHeight()));

            totalVolume = totalVolume.add(oneItemVolume.multiply(qty));

            fragile = fragile || product.isFragile();
        }

        return new BookedProductsDto(totalWeight, totalVolume, fragile);
    }

    @Override
    @Transactional
    public void acceptReturn(Map<UUID, Long> products) {
        if (products == null || products.isEmpty()) return;

        List<WarehouseProduct> found = warehouseRepository.findAllById(products.keySet());
        Map<UUID, WarehouseProduct> byId = found.stream()
                .collect(Collectors.toMap(WarehouseProduct::getId, Function.identity()));

        Set<UUID> missing = new HashSet<>(products.keySet());
        missing.removeAll(byId.keySet());
        if (!missing.isEmpty()) {
            throw new NoSpecifiedProductInWarehouseException(missing.iterator().next());
        }

        for (var e : products.entrySet()) {
            long delta = requirePositive(e.getValue(), "returnQuantity");
            WarehouseProduct p = byId.get(e.getKey());

            long current = nvl(p.getQuantity());
            p.setQuantity(Math.addExact(current, delta));
        }

        warehouseRepository.saveAll(byId.values());
    }

    @Override
    @Transactional
    public void shippedToDelivery(ShippedToDeliveryRequest request) {
        if (request == null) throw new IllegalArgumentException("request must not be null");
        if (request.getOrderId() == null) throw new IllegalArgumentException("orderId must not be null");
        if (request.getDeliveryId() == null) throw new IllegalArgumentException("deliveryId must not be null");

        OrderBooking orderBooking = orderBookingRepository.findById(request.getOrderId())
                .orElseThrow(() -> new NoOrderFoundException(request.getOrderId()));

        if (!Objects.equals(orderBooking.getDeliveryId(), request.getDeliveryId())) {
            orderBooking.setDeliveryId(request.getDeliveryId());
        }
    }

    @Override
    @Transactional
    public BookedProductsDto assemblyProductsForOrder(AssemblyProductsForOrderRequest request) {
        if (request == null || request.getProducts() == null || request.getProducts().isEmpty()) {
            throw new IllegalArgumentException("products must not be empty");
        }

        Set<UUID> ids = request.getProducts().keySet();

        List<WarehouseProduct> found = warehouseRepository.findAllById(ids);

        Map<UUID, WarehouseProduct> byId = found.stream()
                .collect(Collectors.toMap(WarehouseProduct::getId, Function.identity()));

        Set<UUID> missing = new HashSet<>(ids);
        missing.removeAll(byId.keySet());
        if (!missing.isEmpty()) {
            throw new NoSpecifiedProductInWarehouseException(missing.iterator().next());
        }

        BigDecimal weight = BigDecimal.ZERO;
        BigDecimal volume = BigDecimal.ZERO;
        boolean fragile = false;

        for (var e : request.getProducts().entrySet()) {
            UUID productId = e.getKey();
            long reqQty = requirePositive(e.getValue(), "requestQuantity");

            WarehouseProduct p = byId.get(productId);
            long whQty = nvl(p.getQuantity());

            if (whQty < reqQty) {
                throw new ProductInShoppingCartLowQuantityInWarehouse(productId);
            }
            p.setQuantity(whQty - reqQty);

            weight = weight.add(nvl(p.getWeight()).multiply(BigDecimal.valueOf(reqQty)));

            BigDecimal itemVolume = nvl(p.getDepth())
                    .multiply(nvl(p.getWidth()))
                    .multiply(nvl(p.getHeight()));

            volume = volume.add(itemVolume.multiply(BigDecimal.valueOf(reqQty)));

            fragile |= p.isFragile();
        }

        warehouseRepository.saveAll(byId.values());
        orderBookingRepository.save(mapper.toOrderBooking(request));

        return new BookedProductsDto(weight, volume, fragile);
    }

    private static long requirePositive(Long value, String field) {
        if (value == null) throw new IllegalArgumentException(field + " must not be null");
        if (value <= 0) throw new IllegalArgumentException(field + " must be > 0");
        return value;
    }

    private static BigDecimal nvl(BigDecimal v) {
        return v == null ? BigDecimal.ZERO : v;
    }

    private static long nvl(Long v) {
        return v == null ? 0L : v;
    }
}