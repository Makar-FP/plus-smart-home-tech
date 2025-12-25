package ru.yandex.practicum.commerce.shoppingstore.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.commerce.interactionapi.dto.common.ProductCategory;
import ru.yandex.practicum.commerce.interactionapi.dto.store.ProductDto;
import ru.yandex.practicum.commerce.interactionapi.dto.common.ProductQuantityState;
import ru.yandex.practicum.commerce.interactionapi.dto.common.ProductState;
import ru.yandex.practicum.commerce.interactionapi.exception.ProductNotFoundException;
import ru.yandex.practicum.commerce.shoppingstore.mapper.ProductMapper;
import ru.yandex.practicum.commerce.shoppingstore.model.Product;
import ru.yandex.practicum.commerce.shoppingstore.repo.ProductRepository;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ShoppingStoreServiceImpl implements ShoppingStoreService {

    private final ProductRepository productRepository;
    private final ProductMapper mapper;

    @Override
    public ProductDto createNewProduct(ProductDto productDto) {
        Product product = mapper.toProduct(productDto);
        Product savedProduct = productRepository.save(product);
        return mapper.toProductDto(savedProduct);
    }

    @Override
    public ProductDto getProduct(UUID productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException(productId));
        return mapper.toProductDto(product);
    }

    @Override
    public Page<ProductDto> getProducts(ProductCategory category, Pageable pageable) {
        return productRepository
                .findAllByProductCategory(category.toString(), pageable)
                .map(mapper::toProductDto);
    }

    @Override
    public ProductDto updateProduct(ProductDto productDto) {
        productRepository.findById(productDto.getProductId())
                .orElseThrow(() -> new ProductNotFoundException(productDto.getProductId()));

        Product updatedProduct = mapper.toProduct(productDto);
        Product savedProduct = productRepository.save(updatedProduct);
        return mapper.toProductDto(savedProduct);
    }

    @Override
    public boolean removeProduct(UUID productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException(productId));

        product.setProductState(ProductState.DEACTIVATE.toString());
        productRepository.save(product);
        return true;
    }

    @Override
    public boolean setProductQuantityState(UUID productId, ProductQuantityState state) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException(productId));

        product.setQuantityState(state.toString());
        productRepository.save(product);
        return true;
    }
}

