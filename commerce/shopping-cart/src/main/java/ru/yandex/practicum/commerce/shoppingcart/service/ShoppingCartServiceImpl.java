package ru.yandex.practicum.commerce.shoppingcart.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.commerce.interactionapi.client.WarehouseClient;
import ru.yandex.practicum.commerce.interactionapi.dto.cart.ChangeProductQuantityRequest;
import ru.yandex.practicum.commerce.interactionapi.dto.cart.ShoppingCartDto;
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
        List<ShoppingCart> existing = shoppingCartRepository.findAllByUsername(username);
        UUID cartId = existing.isEmpty() ? UUID.randomUUID() : existing.get(0).getId();

        for (var entry : products.entrySet()) {
            UUID productId = entry.getKey();
            Long qtyObj = entry.getValue();
            long qty = (qtyObj == null ? 0L : qtyObj);

            if (qty <= 0) continue;

            ShoppingCart item = shoppingCartRepository.findByUsernameAndProductId(username, productId)
                    .orElseGet(() -> new ShoppingCart(cartId, productId, username, 0L));

            item.setQuantity(qty);

            shoppingCartRepository.save(item);
        }

        List<ShoppingCart> updated = shoppingCartRepository.findAllByUsername(username);
        ShoppingCartDto dto = updated.isEmpty()
                ? new ShoppingCartDto(cartId, username, Map.of())
                : mapper.toShoppingCartDto(updated);

        warehouseClient.checkProductQuantityEnoughForShoppingCart(dto);

        return dto;
    }

    @Override
    @Transactional
    public ShoppingCartDto changeProductQuantity(String username, ChangeProductQuantityRequest request) {
        if (request == null || request.getProductId() == null || request.getNewQuantity() == null) {
            throw new IllegalArgumentException("productId and newQuantity are required");
        }

        ShoppingCart item = shoppingCartRepository.findByUsernameAndProductId(username, request.getProductId())
                .orElseThrow(() -> new ProductNotFoundException(request.getProductId()));

        item.setQuantity(request.getNewQuantity());
        shoppingCartRepository.save(item);

        return getShoppingCart(username);
    }

    @Override
    @Transactional
    public ShoppingCartDto removeFromShoppingCart(String username, List<UUID> productIds) {
        if (productIds == null || productIds.isEmpty()) {
            return getShoppingCart(username);
        }

        UUID cartId = null;

        for (UUID productId : productIds) {
            ShoppingCart item = shoppingCartRepository.findByUsernameAndProductId(username, productId)
                    .orElseThrow(() -> new ProductNotFoundException(productId));

            cartId = item.getId();
            shoppingCartRepository.delete(item);
        }

        if (cartId == null) {
            return new ShoppingCartDto(UUID.randomUUID(), username, Map.of());
        }

        List<ShoppingCart> rest = shoppingCartRepository.findAllById(cartId);
        if (rest.isEmpty()) {
            return new ShoppingCartDto(cartId, username, Map.of());
        }
        return mapper.toShoppingCartDto(rest);
    }

    @Override
    @Transactional
    public boolean deactivateCurrentShoppingCart(String username) {
        shoppingCartRepository.deleteAllByUsername(username);
        return true;
    }
}
