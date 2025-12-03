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

import java.security.SecureRandom;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class WarehouseServiceImpl implements WarehouseService {

    private static final String[] ADDRESSES = new String[]{"ADDRESS_1", "ADDRESS_2"};
    private static final String CURRENT_ADDRESS =
            ADDRESSES[Random.from(new SecureRandom()).nextInt(0, 1)];

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

        product.setQuantity(request.getQuantity());
        WarehouseProduct updatedProduct = warehouseRepository.save(product);

        return mapper.toWarehouseDto(updatedProduct);
    }

    @Override
    public AddressDto getWarehouseAddress() {
        return mapper.toAddressDto(CURRENT_ADDRESS);
    }

    @Override
    public BookedProductsDto checkProductQuantityEnoughForShoppingCart(ShoppingCartDto request) {
        Double weight = 0D;
        Double volume = 0D;
        boolean fragile = false;

        Map<UUID, Long> products = request.getProducts();

        for (Map.Entry<UUID, Long> entry : products.entrySet()) {
            UUID productId = entry.getKey();
            Long cartQuantity = entry.getValue();

            Optional<WarehouseProduct> optionalProduct = warehouseRepository.findById(productId);
            if (optionalProduct.isEmpty()) {
                throw new NoSpecifiedProductInWarehouseException(productId);
            }

            WarehouseProduct product = optionalProduct.get();
            Long warehouseQuantity = product.getQuantity();

            if (warehouseQuantity < cartQuantity) {
                throw new ProductInShoppingCartLowQuantityInWarehouse(productId);
            }

            weight += product.getWeight() * cartQuantity;
            volume += product.getDepth() * product.getWidth() * product.getHeight() * cartQuantity;
            fragile = fragile || product.isFragile();
        }

        return new BookedProductsDto(weight, volume, fragile);
    }
}

