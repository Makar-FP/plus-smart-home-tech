package ru.yandex.practicum.commerce.order.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.yandex.practicum.commerce.order.model.Order;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface OrderRepository extends JpaRepository<Order, UUID> {

    Optional<List<Order>> findByUsername(String username);
}