package ru.yandex.practicum.commerce.payment.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.yandex.practicum.commerce.payment.model.Payment;

import java.util.UUID;

public interface PaymentRepository extends JpaRepository<Payment, UUID> {

}
