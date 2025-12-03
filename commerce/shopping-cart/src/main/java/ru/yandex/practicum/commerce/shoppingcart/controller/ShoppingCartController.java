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
    public ShoppingCartDto getShoppingCart(String username)
            throws NotAuthorizedUserException {
        log.info("GET request to get shopping cart for username={}", username);
        ShoppingCartDto cart = shoppingCartService.getShoppingCart(username);
        log.info("GET response with cart={}", cart);
        return cart;
    }

    @Override
    public ShoppingCartDto addProductToShoppingCart(String username, Map<UUID, Long> products)
            throws NotAuthorizedUserException {
        log.info("PUT request to add products to shopping cart for username={} with products={}",
                username, products);
        ShoppingCartDto cart = shoppingCartService.addProductToShoppingCart(username, products);
        log.info("PUT response with cart={}", cart);
        return cart;
    }

    @Override
    public ShoppingCartDto changeProductQuantity(String username, ChangeProductQuantityRequest request) {
        log.info("POST request to change product quantity in shopping cart for username={} with request={}",
                username, request);
        ShoppingCartDto cart = shoppingCartService.changeProductQuantity(username, request);
        log.info("POST response with cart={}", cart);
        return cart;
    }

    @Override
    public ShoppingCartDto removeFromShoppingCart(String username, List<UUID> productIds) {
        log.info("POST request to remove products from shopping cart for username={} with productIds={}",
                username, productIds);
        ShoppingCartDto cart = shoppingCartService.removeFromShoppingCart(username, productIds);
        log.info("POST response with cart={}", cart);
        return cart;
    }

    @Override
    public boolean deactivateCurrentShoppingCart(String username) {
        log.info("DELETE request to deactivate current shopping cart for username={}", username);
        boolean result = shoppingCartService.deactivateCurrentShoppingCart(username);
        log.info("DELETE response with result={}", result);
        return result;
    }
}

