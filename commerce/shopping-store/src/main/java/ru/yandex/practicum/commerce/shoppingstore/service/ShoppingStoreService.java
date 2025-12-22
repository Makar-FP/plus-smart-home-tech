package ru.yandex.practicum.commerce.shoppingstore.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import ru.yandex.practicum.commerce.interactionapi.dto.ProductDto;
import ru.yandex.practicum.commerce.interactionapi.dto.ProductCategory;
import ru.yandex.practicum.commerce.interactionapi.dto.ProductQuantityState;

import java.util.UUID;

public interface ShoppingStoreService {
    ProductDto createNewProduct(ProductDto product);

    ProductDto getProduct(UUID productId);

    Page<ProductDto> getProducts(ProductCategory category, Pageable pageable);

    ProductDto updateProduct(ProductDto productDto);

    boolean removeProduct(UUID productId);

    boolean setProductQuantityState(UUID productId, ProductQuantityState state);
}
