package ru.yandex.practicum.commerce.shoppingstore.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.commerce.interactionapi.dto.PageDto;
import ru.yandex.practicum.commerce.interactionapi.dto.ProductCategory;
import ru.yandex.practicum.commerce.interactionapi.dto.ProductDto;
import ru.yandex.practicum.commerce.interactionapi.dto.ProductQuantityState;
import ru.yandex.practicum.commerce.interactionapi.dto.SortDto;
import ru.yandex.practicum.commerce.interactionapi.exception.CreateNewProductSericeException;
import ru.yandex.practicum.commerce.interactionapi.operation.ShoppingStoreOperation;
import ru.yandex.practicum.commerce.shoppingstore.service.ShoppingStoreService;

import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/api/v1/shopping-store")
public class ShoppingStoreController implements ShoppingStoreOperation {

    private final ShoppingStoreService shoppingStoreService;

    @Override
    public ProductDto createNewProduct(@Valid ProductDto product) throws CreateNewProductSericeException {
        log.info("PUT request to create new product: {}", product);
        ProductDto newProduct = shoppingStoreService.createNewProduct(product);
        log.info("PUT response with created product: {}", newProduct);
        return newProduct;
    }

    @Override
    public ProductDto updateProduct(@Valid ProductDto product) {
        log.info("POST request to update product: {}", product);
        ProductDto updatedProduct = shoppingStoreService.updateProduct(product);
        log.info("POST response with updated product: {}", updatedProduct);
        return updatedProduct;
    }

    @Override
    public ProductDto getProduct(UUID productId) {
        log.info("GET request for product with id={}", productId);
        ProductDto product = shoppingStoreService.getProduct(productId);
        log.info("GET response with product: {}", product);
        return product;
    }

    @Override
    public PageDto<ProductDto> getProducts(ProductCategory category, Pageable pageable) {
        log.info("GET request for products with category={}", category);
        Page<ProductDto> products = shoppingStoreService.getProducts(category, pageable);
        log.info("GET response with products page: {}", products);

        var sortOrders = pageable.getSort().toList();
        SortDto sortDto = new SortDto(
                sortOrders.get(0).getProperty(),
                sortOrders.get(0).getDirection().toString()
        );

        return new PageDto<>(products.getContent(), List.of(sortDto));
    }

    @Override
    public boolean setProductQuantityState(UUID productId, ProductQuantityState quantityState) {
        log.info("POST request to set product quantity state for productId={} with quantityState={}",
                productId, quantityState);
        boolean result = shoppingStoreService.setProductQuantityState(productId, quantityState);
        log.info("POST response for setting product quantity state: {}", result);
        return false;
    }

    @Override
    public boolean removeProductFromStore(@Valid UUID productId) {
        log.info("POST request to remove product from store with productId={}", productId);
        boolean result = shoppingStoreService.removeProduct(productId);
        log.info("POST response for removing product from store: {}", result);
        return result;
    }
}






