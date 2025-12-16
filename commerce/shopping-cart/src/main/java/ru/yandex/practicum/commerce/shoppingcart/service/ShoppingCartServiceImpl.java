package ru.yandex.practicum.commerce.shoppingcart.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.commerce.interactionapi.client.WarehouseClient;
import ru.yandex.practicum.commerce.interactionapi.dto.ChangeProductQuantityRequest;
import ru.yandex.practicum.commerce.interactionapi.dto.ShoppingCartDto;
import ru.yandex.practicum.commerce.interactionapi.exception.ProductNotFoundException;
import ru.yandex.practicum.commerce.shoppingcart.mapper.ShoppingCartMapper;
import ru.yandex.practicum.commerce.shoppingcart.model.ShoppingCart;
import ru.yandex.practicum.commerce.shoppingcart.repo.ShoppingCartRepository;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ShoppingCartServiceImpl implements ShoppingCartService {

    private final ShoppingCartRepository shoppingCartRepository;
    private final ShoppingCartMapper mapper;
    private final WarehouseClient warehouseClient;

    @Override
    public ShoppingCartDto getShoppingCart(String username) {
        List<ShoppingCart> items = shoppingCartRepository.findAllByUsername(username);
        if (items.isEmpty()) {
            return ShoppingCartDto.builder()
                    .shoppingCartId(null)
                    .username(username)
                    .products(Map.of())
                    .build();
        }
        return mapper.toShoppingCartDto(items);
    }

    @Override
    @org.springframework.transaction.annotation.Transactional
    public ShoppingCartDto addProductToShoppingCart(String username, Map<UUID, Long> products) {
        if (products == null || products.isEmpty()) {
            return getShoppingCart(username);
        }

        List<ShoppingCart> existing = shoppingCartRepository.findAllByUsername(username);
        UUID cartId = existing.isEmpty() ? UUID.randomUUID() : existing.get(0).getShoppingCartId();

        for (Map.Entry<UUID, Long> e : products.entrySet()) {
            UUID productId = e.getKey();
            Long qty = e.getValue();

            if (productId == null || qty == null || qty <= 0) {
                throw new IllegalArgumentException("Quantity must be > 0");
            }

            ShoppingCart item = shoppingCartRepository.findByUsernameAndProductId(username, productId)
                    .orElseGet(() -> {
                        ShoppingCart sc = new ShoppingCart();
                        sc.setShoppingCartId(cartId);
                        sc.setUsername(username);
                        sc.setProductId(productId);
                        sc.setQuantity(0L);
                        return sc;
                    });

            item.setShoppingCartId(cartId);
            item.setQuantity(qty);

            shoppingCartRepository.save(item);
        }

        ShoppingCartDto dto = getShoppingCart(username);

        warehouseClient.checkProductQuantityEnoughForShoppingCart(dto);

        return dto;
    }

    @Override
    @org.springframework.transaction.annotation.Transactional
    public ShoppingCartDto changeProductQuantity(String username, ChangeProductQuantityRequest request) {
        if (request == null || request.getProductId() == null || request.getNewQuantity() == null) {
            throw new IllegalArgumentException("Invalid request");
        }
        if (request.getNewQuantity() <= 0) {
            throw new IllegalArgumentException("newQuantity must be > 0");
        }

        ShoppingCart item = shoppingCartRepository
                .findByUsernameAndProductId(username, request.getProductId())
                .orElseThrow(() -> new ProductNotFoundException(request.getProductId()));

        item.setQuantity(request.getNewQuantity());
        shoppingCartRepository.save(item);

        return getShoppingCart(username);
    }

    @Override
    @org.springframework.transaction.annotation.Transactional
    public ShoppingCartDto removeFromShoppingCart(String username, List<UUID> productIds) {
        if (productIds == null || productIds.isEmpty()) {
            return getShoppingCart(username);
        }

        UUID cartId = null;

        for (UUID productId : productIds) {
            ShoppingCart item = shoppingCartRepository
                    .findByUsernameAndProductId(username, productId)
                    .orElseThrow(() -> new ProductNotFoundException(productId));

            cartId = item.getShoppingCartId();
            shoppingCartRepository.delete(item);
        }

        List<ShoppingCart> left = (cartId == null) ? List.of() : shoppingCartRepository.findAllByShoppingCartId(cartId);

        if (left.isEmpty()) {
            return ShoppingCartDto.builder()
                    .shoppingCartId(cartId)
                    .username(username)
                    .products(Map.of())
                    .build();
        }
        return mapper.toShoppingCartDto(left);
    }

    @Override
    @org.springframework.transaction.annotation.Transactional
    public boolean deactivateCurrentShoppingCart(String username) {
        shoppingCartRepository.deleteAllByUsername(username);
        return true;
    }
}
