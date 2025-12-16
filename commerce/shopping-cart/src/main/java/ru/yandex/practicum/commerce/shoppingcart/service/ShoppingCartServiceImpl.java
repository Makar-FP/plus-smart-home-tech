package ru.yandex.practicum.commerce.shoppingcart.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.commerce.interactionapi.client.WarehouseClient;
import ru.yandex.practicum.commerce.interactionapi.dto.BookedProductsDto;
import ru.yandex.practicum.commerce.interactionapi.dto.ChangeProductQuantityRequest;
import ru.yandex.practicum.commerce.interactionapi.dto.ShoppingCartDto;
import ru.yandex.practicum.commerce.interactionapi.exception.ProductNotFoundException;
import ru.yandex.practicum.commerce.shoppingcart.mapper.ShoppingCartMapper;
import ru.yandex.practicum.commerce.shoppingcart.model.ShoppingCart;
import ru.yandex.practicum.commerce.shoppingcart.repo.ShoppingCartRepository;

import java.nio.charset.StandardCharsets;
import java.util.*;

@Service
@RequiredArgsConstructor
public class ShoppingCartServiceImpl implements ShoppingCartService {

    private final ShoppingCartRepository shoppingCartRepository;
    private final ShoppingCartMapper mapper;
    private final WarehouseClient warehouseClient;

    @Override
    public ShoppingCartDto getShoppingCart(String username) {
        List<ShoppingCart> items = shoppingCartRepository.findByUsername(username)
                .orElseGet(List::of);

        if (items.isEmpty()) {
            return new ShoppingCartDto(cartIdForUsername(username), username, Map.of());
        }

        return mapper.toShoppingCartDto(items);
    }

    @Override
    @Transactional
    public ShoppingCartDto addProductToShoppingCart(String username, Map<UUID, Long> productsToAdd) {
        if (productsToAdd == null || productsToAdd.isEmpty()) {
            return getShoppingCart(username);
        }

        List<ShoppingCart> existing = shoppingCartRepository.findByUsername(username)
                .orElseGet(List::of);

        UUID cartId = existing.isEmpty()
                ? cartIdForUsername(username)
                : existing.getFirst().getShoppingCartId();

        Map<UUID, Long> merged = new HashMap<>();
        for (ShoppingCart row : existing) {
            merged.put(row.getProductId(), nvl(row.getQuantity()));
        }

        for (Map.Entry<UUID, Long> e : productsToAdd.entrySet()) {
            UUID productId = e.getKey();
            long addQty = nvl(e.getValue());
            if (addQty <= 0) continue;

            merged.merge(productId, addQty, Long::sum);
        }

        ShoppingCartDto cartToCheck = new ShoppingCartDto(cartId, username, merged);

        BookedProductsDto ignored = warehouseClient.checkProductQuantityEnoughForShoppingCart(cartToCheck);

        for (Map.Entry<UUID, Long> e : merged.entrySet()) {
            UUID productId = e.getKey();
            long qty = nvl(e.getValue());

            ShoppingCart row = shoppingCartRepository.findByUsernameAndProductId(username, productId)
                    .orElseGet(ShoppingCart::new);

            row.setShoppingCartId(cartId);
            row.setUsername(username);
            row.setProductId(productId);
            row.setQuantity(qty);

            shoppingCartRepository.save(row);
        }

        return new ShoppingCartDto(cartId, username, merged);
    }

    @Override
    @Transactional
    public ShoppingCartDto changeProductQuantity(String username, ChangeProductQuantityRequest request) {
        UUID productId = request.getProductId();
        long newQty = nvl(request.getNewQuantity());

        ShoppingCart row = shoppingCartRepository.findByUsernameAndProductId(username, productId)
                .orElseThrow(() -> new ProductNotFoundException(productId));

        if (newQty <= 0) {
            shoppingCartRepository.delete(row);
            return getShoppingCart(username);
        }

        row.setQuantity(newQty);
        shoppingCartRepository.save(row);

        return getShoppingCart(username);
    }

    @Override
    @Transactional
    public ShoppingCartDto removeFromShoppingCart(String username, List<UUID> productIds) {
        if (productIds == null || productIds.isEmpty()) {
            return getShoppingCart(username);
        }

        for (UUID productId : productIds) {
            ShoppingCart row = shoppingCartRepository.findByUsernameAndProductId(username, productId)
                    .orElseThrow(() -> new ProductNotFoundException(productId));
            shoppingCartRepository.delete(row);
        }

        return getShoppingCart(username);
    }

    @Override
    @Transactional
    public boolean deactivateCurrentShoppingCart(String username) {
        List<ShoppingCart> items = shoppingCartRepository.findByUsername(username)
                .orElseGet(List::of);

        for (ShoppingCart row : items) {
            shoppingCartRepository.delete(row);
        }
        return true;
    }

    private static long nvl(Long v) {
        return v == null ? 0L : v;
    }

    private static UUID cartIdForUsername(String username) {
        return UUID.nameUUIDFromBytes(("cart:" + username).getBytes(StandardCharsets.UTF_8));
    }
}
