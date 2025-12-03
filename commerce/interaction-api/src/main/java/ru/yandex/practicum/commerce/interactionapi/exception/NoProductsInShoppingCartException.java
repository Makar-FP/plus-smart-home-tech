package ru.yandex.practicum.commerce.interactionapi.exception;

public class NoProductsInShoppingCartException extends RuntimeException {
    public NoProductsInShoppingCartException(String username) {
        super("Shopping cart for user " + username + " was not found");
    }
}
