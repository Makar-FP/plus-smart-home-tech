package ru.yandex.practicum.commerce.shoppingstore.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import ru.yandex.practicum.commerce.interactionapi.dto.store.ProductDto;
import ru.yandex.practicum.commerce.interactionapi.dto.common.ProductCategory;
import ru.yandex.practicum.commerce.interactionapi.dto.common.ProductQuantityState;

import java.util.UUID;

public interface ShoppingStoreService {
    ProductDto createNewProduct(ProductDto product);

    ProductDto getProduct(UUID productId);

    Page<ProductDto> getProducts(ProductCategory category, Pageable pageable);

    ProductDto updateProduct(ProductDto productDto);

    boolean removeProduct(UUID productId);

    boolean setProductQuantityState(UUID productId, ProductQuantityState state);
}
