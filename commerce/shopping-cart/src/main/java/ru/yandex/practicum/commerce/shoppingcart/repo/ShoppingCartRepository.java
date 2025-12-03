package ru.yandex.practicum.commerce.shoppingcart.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.yandex.practicum.commerce.shoppingcart.model.ShoppingCart;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ShoppingCartRepository extends JpaRepository<ShoppingCart, UUID> {

    Optional<List<ShoppingCart>> findByUsername(String username);

    Optional<ShoppingCart> findByProductId(UUID productId);

    Optional<ShoppingCart> findByUsernameAndProductId(String username, UUID productId);

    Optional<List<ShoppingCart>> findByShoppingCartId(UUID shoppingCartId);
}
