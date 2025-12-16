package ru.yandex.practicum.commerce.shoppingcart.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.commerce.interactionapi.dto.ChangeProductQuantityRequest;
import ru.yandex.practicum.commerce.interactionapi.dto.ShoppingCartDto;
import ru.yandex.practicum.commerce.interactionapi.exception.NotAuthorizedUserException;
import ru.yandex.practicum.commerce.interactionapi.operation.ShoppingCartOperation;
import ru.yandex.practicum.commerce.shoppingcart.service.ShoppingCartService;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/api/v1/shopping-cart")
public class ShoppingCartController implements ShoppingCartOperation {

    private final ShoppingCartService shoppingCartService;

    @Override
    public ShoppingCartDto getShoppingCart(String username) throws NotAuthorizedUserException {
        log.info("--> GET username={}", username);
        ShoppingCartDto cart = shoppingCartService.getShoppingCart(username);
        log.info("<-- GET cart={}", cart);
        return cart;
    }

    @Override
    public ShoppingCartDto addProductToShoppingCart(String username, Map<UUID, Long> products)
            throws NotAuthorizedUserException {
        log.info("--> PUT username={} products={}", username, products);
        ShoppingCartDto cart = shoppingCartService.addProductToShoppingCart(username, products);
        log.info("<-- PUT cart={}", cart);
        return cart;
    }

    @Override
    public ShoppingCartDto changeProductQuantity(String username, ChangeProductQuantityRequest request) {
        log.info("--> POST change qty: {}", request);
        ShoppingCartDto cart = shoppingCartService.changeProductQuantity(username, request);
        log.info("<-- POST cart={}", cart);
        return cart;
    }

    @Override
    public ShoppingCartDto removeFromShoppingCart(String username, List<UUID> productIds) {
        log.info("--> POST remove: {}", productIds);
        ShoppingCartDto cart = shoppingCartService.removeFromShoppingCart(username, productIds);
        log.info("<-- POST cart={}", cart);
        return cart;
    }

    @Override
    public boolean deactivateCurrentShoppingCart(String username) {
        log.info("--> DELETE username={}", username);
        boolean result = shoppingCartService.deactivateCurrentShoppingCart(username);
        log.info("<-- DELETE result={}", result);
        return result;
    }
}
