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
            throw new NoProductsInShoppingCartException(username);
        }
        return mapper.toShoppingCartDto(items);
    }

    @Override
    @Transactional
    public ShoppingCartDto addProductToShoppingCart(String username, Map<UUID, Long> products) {
        List<ShoppingCart> existing = shoppingCartRepository.findAllByUsername(username);

        UUID cartId = existing.isEmpty()
                ? UUID.randomUUID()
                : existing.get(0).getShoppingCartId();

        ShoppingCartDto candidate = new ShoppingCartDto(cartId, username, products);
        warehouseClient.checkProductQuantityEnoughForShoppingCart(candidate);

        for (var entry : products.entrySet()) {
            UUID productId = entry.getKey();
            Long qty = entry.getValue();

            ShoppingCart item = shoppingCartRepository
                    .findByUsernameAndProductId(username, productId)
                    .orElseGet(() -> new ShoppingCart(cartId, productId, username, 0L));

            item.setShoppingCartId(cartId);
            item.setUsername(username);
            item.setProductId(productId);
            item.setQuantity(qty);
            shoppingCartRepository.save(item);
        }

        return getShoppingCart(username);
    }

    @Override
    @Transactional
    public ShoppingCartDto changeProductQuantity(String username, ChangeProductQuantityRequest request) {
        ShoppingCart item = shoppingCartRepository
                .findByUsernameAndProductId(username, request.getProductId())
                .orElseThrow(() -> new ProductNotFoundException(request.getProductId()));

        Long newQty = request.getNewQuantity();
        if (newQty == null || newQty <= 0) {
            shoppingCartRepository.delete(item);
            List<ShoppingCart> left = shoppingCartRepository.findAllByShoppingCartId(item.getShoppingCartId());
            if (left.isEmpty()) {
                return new ShoppingCartDto(item.getShoppingCartId(), username, Map.of());
            }
            return mapper.toShoppingCartDto(left);
        }

        item.setQuantity(newQty);
        shoppingCartRepository.save(item);

        return getShoppingCart(username);
    }

    @Override
    @Transactional
    public ShoppingCartDto removeFromShoppingCart(String username, List<UUID> productIds) {
        if (productIds == null || productIds.isEmpty()) {
            return getShoppingCart(username);
        }

        List<ShoppingCart> existing = shoppingCartRepository.findAllByUsername(username);
        if (existing.isEmpty()) {
            throw new NoProductsInShoppingCartException(username);
        }
        UUID cartId = existing.get(0).getShoppingCartId();

        for (UUID productId : productIds) {
            ShoppingCart item = shoppingCartRepository
                    .findByUsernameAndProductId(username, productId)
                    .orElseThrow(() -> new ProductNotFoundException(productId));
            shoppingCartRepository.delete(item);
        }

        List<ShoppingCart> left = shoppingCartRepository.findAllByShoppingCartId(cartId);
        if (left.isEmpty()) {
            return new ShoppingCartDto(cartId, username, Map.of());
        }
        return mapper.toShoppingCartDto(left);
    }

    @Override
    @Transactional
    public boolean deactivateCurrentShoppingCart(String username) {
        int deleted = shoppingCartRepository.deleteAllByUsername(username);
        if (deleted == 0) {
            throw new NoProductsInShoppingCartException(username);
        }
        return true;
    }
}
