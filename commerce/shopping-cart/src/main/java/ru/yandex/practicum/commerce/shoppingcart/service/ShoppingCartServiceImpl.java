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
            return new ShoppingCartDto(UUID.randomUUID(), username, Map.of());
        }
        return mapper.toShoppingCartDto(items);
    }

    @Override
    @Transactional
    public ShoppingCartDto addProductToShoppingCart(String username, Map<UUID, Long> products) {
        if (products == null || products.isEmpty()) {
            return getShoppingCart(username);
        }

        List<ShoppingCart> existing = shoppingCartRepository.findAllByUsername(username);
        UUID cartId = existing.isEmpty()
                ? UUID.randomUUID()
                : existing.get(0).getShoppingCartId();

        for (var e : products.entrySet()) {
            UUID productId = e.getKey();
            Long qty = e.getValue();

            if (productId == null || qty == null || qty <= 0) {
                throw new IllegalArgumentException("Invalid productId/quantity");
            }

            ShoppingCart item = shoppingCartRepository
                    .findByUsernameAndProductId(username, productId)
                    .orElseGet(() -> new ShoppingCart(cartId, productId, username, 0L));

            item.setQuantity(item.getQuantity() + qty);

            item.setShoppingCartId(cartId);
            item.setUsername(username);
            item.setProductId(productId);

            shoppingCartRepository.save(item);
        }

        ShoppingCartDto dto = mapper.toShoppingCartDto(shoppingCartRepository.findAllByUsername(username));

        warehouseClient.checkProductQuantityEnoughForShoppingCart(dto);

        return dto;
    }

    @Override
    @Transactional
    public ShoppingCartDto changeProductQuantity(String username, ChangeProductQuantityRequest request) {
        if (request == null || request.getProductId() == null || request.getNewQuantity() == null || request.getNewQuantity() <= 0) {
            throw new IllegalArgumentException("Invalid request");
        }

        ShoppingCart item = shoppingCartRepository
                .findByUsernameAndProductId(username, request.getProductId())
                .orElseThrow(() -> new ProductNotFoundException(request.getProductId()));

        item.setQuantity(request.getNewQuantity());
        shoppingCartRepository.save(item);

        return mapper.toShoppingCartDto(shoppingCartRepository.findAllByUsername(username));
    }

    @Override
    @Transactional
    public ShoppingCartDto removeFromShoppingCart(String username, List<UUID> productIds) {
        List<ShoppingCart> all = shoppingCartRepository.findAllByUsername(username);
        if (all.isEmpty()) {
            return new ShoppingCartDto(UUID.randomUUID(), username, Map.of());
        }
        UUID cartId = all.get(0).getShoppingCartId();

        for (UUID productId : productIds) {
            ShoppingCart item = shoppingCartRepository
                    .findByUsernameAndProductId(username, productId)
                    .orElseThrow(() -> new ProductNotFoundException(productId));
            shoppingCartRepository.delete(item);
        }

        List<ShoppingCart> remaining = shoppingCartRepository.findAllByShoppingCartId(cartId);
        if (remaining.isEmpty()) {
            return new ShoppingCartDto(cartId, username, Map.of());
        }
        return mapper.toShoppingCartDto(remaining);
    }

    @Override
    @Transactional
    public boolean deactivateCurrentShoppingCart(String username) {
        shoppingCartRepository.deleteAllByUsername(username);
        return true;
    }
}