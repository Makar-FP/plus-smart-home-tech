package ru.yandex.practicum.commerce.shoppingcart.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
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
@RequestMapping("/api/v1/shopping-cart")
public class ShoppingCartController implements ShoppingCartOperation {

    private final ShoppingCartService shoppingCartService;

    @Override
    @GetMapping
    public ShoppingCartDto getShoppingCart(@RequestParam String username) {
        log.info("--> GET username={}", username);
        return shoppingCartService.getShoppingCart(username);
    }

    @Override
    @PutMapping
    public ShoppingCartDto addProductToShoppingCart(@RequestParam String username,
                                                    @RequestBody Map<UUID, Long> products) {
        log.info("--> PUT username={} products={}", username, products);
        return shoppingCartService.addProductToShoppingCart(username, products);
    }

    @Override
    @PostMapping("/change-quantity")
    public ShoppingCartDto changeProductQuantity(@RequestParam String username,
                                                 @RequestBody ChangeProductQuantityRequest request) {
        log.info("--> POST change qty username={} request={}", username, request);
        return shoppingCartService.changeProductQuantity(username, request);
    }

    @Override
    @PostMapping("/remove")
    public ShoppingCartDto removeFromShoppingCart(@RequestParam String username,
                                                  @RequestBody List<UUID> productIds) {
        log.info("--> POST remove username={} productIds={}", username, productIds);
        return shoppingCartService.removeFromShoppingCart(username, productIds);
    }

    @Override
    @DeleteMapping
    public boolean deactivateCurrentShoppingCart(@RequestParam String username) {
        log.info("--> DELETE username={}", username);
        return shoppingCartService.deactivateCurrentShoppingCart(username);
    }
}
