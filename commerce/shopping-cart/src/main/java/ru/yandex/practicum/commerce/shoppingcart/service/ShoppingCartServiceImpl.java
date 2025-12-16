package ru.yandex.practicum.commerce.shoppingcart.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.commerce.interactionapi.client.WarehouseClient;
import ru.yandex.practicum.commerce.interactionapi.dto.ChangeProductQuantityRequest;
import ru.yandex.practicum.commerce.interactionapi.dto.ShoppingCartDto;
import ru.yandex.practicum.commerce.interactionapi.exception.NoProductsInShoppingCartException;
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
        List<ShoppingCart> items = shoppingCartRepository.findByUsername(username).orElse(List.of());
        if (items.isEmpty()) {
            throw new NoProductsInShoppingCartException(username);
        }
        return mapper.toShoppingCartDto(items);
    }

    @Override
    @Transactional
    public ShoppingCartDto addProductToShoppingCart(String username, Map<UUID, Long> products) {

        List<ShoppingCart> existing = shoppingCartRepository.findByUsername(username).orElse(List.of());

        UUID cartId = existing.isEmpty()
                ? UUID.randomUUID()
                : existing.getFirst().getShoppingCartId();

        ShoppingCartDto cartToCheck = new ShoppingCartDto(cartId, username, products);
        warehouseClient.checkProductQuantityEnoughForShoppingCart(cartToCheck);

        for (Map.Entry<UUID, Long> entry : products.entrySet()) {
            ShoppingCart row = new ShoppingCart();
            row.setShoppingCartId(cartId);
            row.setUsername(username);
            row.setProductId(entry.getKey());
            row.setQuantity(entry.getValue());
            shoppingCartRepository.save(row);
        }

        return new ShoppingCartDto(cartId, username, products);
    }

    @Override
    public ShoppingCartDto changeProductQuantity(String username, ChangeProductQuantityRequest request) {
        ShoppingCart cart = shoppingCartRepository.findByProductId(request.getProductId())
                .orElseThrow(() -> new ProductNotFoundException(request.getProductId()));

        cart.setQuantity(request.getNewQuantity());
        shoppingCartRepository.save(cart);

        return getShoppingCart(username);
    }

    @Override
    @Transactional
    public ShoppingCartDto removeFromShoppingCart(String username, List<UUID> productIds) {

        UUID shoppingCartId = null;

        for (UUID productId : productIds) {
            ShoppingCart cart = shoppingCartRepository.findByUsernameAndProductId(username, productId)
                    .orElseThrow(() -> new ProductNotFoundException(productId));

            shoppingCartId = cart.getShoppingCartId();
            shoppingCartRepository.delete(cart);
        }

        List<ShoppingCart> left = shoppingCartRepository.findByShoppingCartId(shoppingCartId).orElse(List.of());
        if (left.isEmpty()) {
            return new ShoppingCartDto(shoppingCartId, username, Map.of());
        }
        return mapper.toShoppingCartDto(left);
    }

    @Override
    public boolean deactivateCurrentShoppingCart(String username) {
        List<ShoppingCart> items = shoppingCartRepository.findByUsername(username)
                .orElseThrow(() -> new NoProductsInShoppingCartException(username));

        for (ShoppingCart cart : items) {
            shoppingCartRepository.delete(cart);
        }
        return true;
    }
}

