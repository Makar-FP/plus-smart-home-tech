package ru.yandex.practicum.commerce.warehouse.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.commerce.interactionapi.dto.*;
import ru.yandex.practicum.commerce.interactionapi.exception.NoSpecifiedProductInWarehouseException;
import ru.yandex.practicum.commerce.interactionapi.exception.ProductInShoppingCartLowQuantityInWarehouse;
import ru.yandex.practicum.commerce.warehouse.mapper.WarehouseMapper;
import ru.yandex.practicum.commerce.warehouse.model.WarehouseProduct;
import ru.yandex.practicum.commerce.warehouse.repo.WarehouseRepository;

import java.math.BigDecimal;
import java.security.SecureRandom;
import java.util.Map;
import java.util.UUID;

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

    private static BigDecimal nvl(BigDecimal v) {
        return v == null ? BigDecimal.ZERO : v;
    }

    private static long nvl(Long v) {
        return v == null ? 0L : v;
    }
}