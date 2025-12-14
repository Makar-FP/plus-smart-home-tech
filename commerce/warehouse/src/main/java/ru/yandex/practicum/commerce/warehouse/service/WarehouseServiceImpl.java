package ru.yandex.practicum.commerce.warehouse.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.commerce.interactionapi.dto.AddProductToWarehouseRequest;
import ru.yandex.practicum.commerce.interactionapi.dto.AddressDto;
import ru.yandex.practicum.commerce.interactionapi.dto.BookedProductsDto;
import ru.yandex.practicum.commerce.interactionapi.dto.NewProductInWarehouseRequest;
import ru.yandex.practicum.commerce.interactionapi.dto.ShoppingCartDto;
import ru.yandex.practicum.commerce.interactionapi.dto.WarehouseDto;
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

    private static final String[] ADDRESSES = {"ADDRESS_1", "ADDRESS_2"};
    private static final String CURRENT_ADDRESS =
            ADDRESSES[new SecureRandom().nextInt(ADDRESSES.length)];

    private final WarehouseRepository warehouseRepository;
    private final WarehouseMapper mapper;

    @Override
    public WarehouseDto newProductInWarehouse(NewProductInWarehouseRequest request) {
        WarehouseProduct product = mapper.toProduct(request);
        WarehouseProduct savedProduct = warehouseRepository.save(product);
        return mapper.toWarehouseDto(savedProduct);
    }

    @Override
    public WarehouseDto addProductToWarehouse(AddProductToWarehouseRequest request) {
        WarehouseProduct product = warehouseRepository.findById(request.getProductId())
                .orElseThrow(() -> new NoSpecifiedProductInWarehouseException(request.getProductId()));

        long current = product.getQuantity() == null ? 0L : product.getQuantity();
        long toAdd = request.getQuantity() == null ? 0L : request.getQuantity();
        product.setQuantity(current + toAdd);

        WarehouseProduct updatedProduct = warehouseRepository.save(product);
        return mapper.toWarehouseDto(updatedProduct);
    }

    @Override
    public AddressDto getWarehouseAddress() {
        return mapper.toAddressDto(CURRENT_ADDRESS);
    }

    @Override
    public BookedProductsDto checkProductQuantityEnoughForShoppingCart(ShoppingCartDto request) {
        BigDecimal weight = BigDecimal.ZERO;
        BigDecimal volume = BigDecimal.ZERO;
        boolean fragile = false;

        for (Map.Entry<UUID, Long> entry : request.getProducts().entrySet()) {
            UUID productId = entry.getKey();
            long cartQuantity = entry.getValue();

            WarehouseProduct product = warehouseRepository.findById(productId)
                    .orElseThrow(() -> new NoSpecifiedProductInWarehouseException(productId));

            long warehouseQuantity = product.getQuantity();
            if (warehouseQuantity < cartQuantity) {
                throw new ProductInShoppingCartLowQuantityInWarehouse(productId);
            }

            BigDecimal qty = BigDecimal.valueOf(cartQuantity);

            weight = weight.add(product.getWeight().multiply(qty));

            BigDecimal itemVolume = product.getDepth()
                    .multiply(product.getWidth())
                    .multiply(product.getHeight());

            volume = volume.add(itemVolume.multiply(qty));

            fragile |= product.isFragile();
        }

        return new BookedProductsDto(weight, volume, fragile);
    }
}
