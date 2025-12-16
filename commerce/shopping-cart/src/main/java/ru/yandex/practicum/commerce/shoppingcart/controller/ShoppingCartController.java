package ru.yandex.practicum.commerce.shoppingcart.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
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

    private static final String USER_HEADER = "X-Sharer-User-Id";
    private final ShoppingCartService shoppingCartService;

    @Override
    public ShoppingCartDto getShoppingCart(
            @RequestHeader(USER_HEADER) String username
    ) throws NotAuthorizedUserException {
        log.info("--> GET запрос c username={}", username);
        ShoppingCartDto cart = shoppingCartService.getShoppingCart(username);
        log.info("<-- GET ответ cart={}", cart);
        return cart;
    }

    @Override
    public ShoppingCartDto addProductToShoppingCart(
            @RequestHeader(USER_HEADER) String username,
            @RequestBody Map<UUID, Long> products
    ) throws NotAuthorizedUserException {
        log.info("--> PUT запрос c username={} и products={}", username, products);
        ShoppingCartDto cart = shoppingCartService.addProductToShoppingCart(username, products);
        log.info("<-- PUT ответ cart={}", cart);
        return cart;
    }

    @Override
    public ShoppingCartDto changeProductQuantity(
            @RequestHeader(USER_HEADER) String username,
            @RequestBody ChangeProductQuantityRequest request
    ) {
        log.info("--> POST запрос на изменение количества продукта в корзине: {}", request);
        ShoppingCartDto cart = shoppingCartService.changeProductQuantity(username, request);
        log.info("<-- POST ответ cart={}", cart);
        return cart;
    }

    @Override
    public ShoppingCartDto removeFromShoppingCart(
            @RequestHeader(USER_HEADER) String username,
            @RequestBody List<UUID> productIds
    ) {
        log.info("--> POST запрос на удаление продуктов из корзины: {}", productIds);
        ShoppingCartDto cart = shoppingCartService.removeFromShoppingCart(username, productIds);
        log.info("<-- POST ответ cart={}", cart);
        return cart;
    }

    @Override
    public boolean deactivateCurrentShoppingCart(
            @RequestHeader(USER_HEADER) String username
    ) {
        log.info("--> DELETE запрос c username={}", username);
        boolean result = shoppingCartService.deactivateCurrentShoppingCart(username);
        log.info("<-- DELETE ответ {} : ", result);
        return result;
    }
}
