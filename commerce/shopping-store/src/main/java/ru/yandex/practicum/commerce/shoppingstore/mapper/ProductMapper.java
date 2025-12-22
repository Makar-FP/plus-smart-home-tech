package ru.yandex.practicum.commerce.shoppingstore.mapper;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.commerce.interactionapi.dto.ProductDto;
import ru.yandex.practicum.commerce.shoppingstore.model.Product;

import java.util.UUID;

@Component
public class ProductMapper {
    public Product toProduct(ProductDto productDto) {
        Product product = new Product();
        product.setId(UUID.randomUUID());
        product.setProductName(productDto.getProductName());
        product.setDescription(productDto.getDescription());
        product.setImageSrc(productDto.getImageSrc());
        product.setProductCategory(productDto.getProductCategory());
        product.setProductState(productDto.getProductState());
        product.setQuantityState(productDto.getQuantityState());
        product.setPrice(productDto.getPrice());

        return product;
    }
    public ProductDto toProductDto(Product product) {
        return new ProductDto(
                product.getId(),
                product.getProductName(),
                product.getDescription(),
                product.getImageSrc(),
                product.getQuantityState(),
                product.getProductState(),
                product.getProductCategory(),
                product.getPrice()
        );
    }
}
