package ru.yandex.practicum.commerce.shoppingcart.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.commerce.shoppingcart.model.CompositeKey;
import ru.yandex.practicum.commerce.shoppingcart.model.ShoppingCart;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ShoppingCartRepository extends JpaRepository<ShoppingCart, CompositeKey> {

    List<ShoppingCart> findAllByUsername(String username);

    Optional<ShoppingCart> findByUsernameAndProductId(String username, UUID productId);

    List<ShoppingCart> findAllById(UUID id);

    @Transactional
    @Modifying
    @Query("delete from ShoppingCart sc where sc.username = :username")
    int deleteAllByUsername(@Param("username") String username);
}

