package ru.yandex.practicum.commerce.payment.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import ru.yandex.practicum.commerce.interactionapi.client.OrderClient;
import ru.yandex.practicum.commerce.interactionapi.client.ShoppingStoreClient;
import ru.yandex.practicum.commerce.interactionapi.dto.order.OrderDto;
import ru.yandex.practicum.commerce.interactionapi.dto.payment.PaymentDto;
import ru.yandex.practicum.commerce.interactionapi.dto.payment.PaymentStatus;
import ru.yandex.practicum.commerce.interactionapi.dto.store.ProductDto;
import ru.yandex.practicum.commerce.interactionapi.exception.NoPaymentFoundException;
import ru.yandex.practicum.commerce.payment.mapper.PaymentMapper;
import ru.yandex.practicum.commerce.payment.model.Payment;
import ru.yandex.practicum.commerce.payment.repo.PaymentRepository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private static final BigDecimal FEE_RATE = new BigDecimal("0.10");
    private static final int MONEY_SCALE = 2;
    private static final RoundingMode MONEY_ROUNDING = RoundingMode.HALF_UP;

    private final PaymentRepository paymentRepository;
    private final PaymentMapper mapper;
    private final ShoppingStoreClient shoppingStoreClient;
    private final OrderClient orderClient;

    @Override
    public PaymentDto payment(OrderDto order) {
        Objects.requireNonNull(order, "order must not be null");
        Payment payment = mapper.toPayment(order);
        Payment saved = paymentRepository.save(payment);
        return mapper.toPaymentDto(saved);
    }

    @Override
    public BigDecimal getTotalCost(OrderDto order) {
        Objects.requireNonNull(order, "order must not be null");

        BigDecimal product = nz(order.getProductPrice());
        BigDecimal delivery = nz(order.getDeliveryPrice());
        BigDecimal fee = product.multiply(FEE_RATE);

        return money(product.add(delivery).add(fee));
    }

    @Override
    public BigDecimal productCost(OrderDto order) {
        Objects.requireNonNull(order, "order must not be null");

        Map<UUID, Long> products = order.getProducts();
        if (products == null || products.isEmpty()) {
            return money(BigDecimal.ZERO);
        }

        BigDecimal total = BigDecimal.ZERO;

        for (Map.Entry<UUID, Long> e : products.entrySet()) {
            UUID productId = e.getKey();
            long qty = e.getValue() == null ? 0L : e.getValue();

            if (productId == null || qty <= 0) continue;

            ProductDto product = shoppingStoreClient.getProduct(productId);
            BigDecimal price = nz(product.getPrice());

            total = total.add(price.multiply(BigDecimal.valueOf(qty)));
        }

        return money(total);
    }

    @Override
    @Transactional
    public void paymentSuccess(UUID paymentId) {
        Payment payment = getPayment(paymentId);

        if (payment.getPaymentStatus() == PaymentStatus.SUCCESS) return;

        payment.setPaymentStatus(PaymentStatus.SUCCESS);
        paymentRepository.save(payment);

        UUID orderId = payment.getOrderId();
        runAfterCommit(() -> orderClient.payment(orderId));
    }

    @Override
    @Transactional
    public void paymentFailed(UUID paymentId) {
        Payment payment = getPayment(paymentId);

        if (payment.getPaymentStatus() == PaymentStatus.FAILED) return;

        payment.setPaymentStatus(PaymentStatus.FAILED);
        paymentRepository.save(payment);

        UUID orderId = payment.getOrderId();
        runAfterCommit(() -> orderClient.paymentFailed(orderId));
    }

    private Payment getPayment(UUID paymentId) {
        Objects.requireNonNull(paymentId, "paymentId must not be null");
        return paymentRepository.findById(paymentId)
                .orElseThrow(() -> new NoPaymentFoundException(paymentId));
    }

    private static BigDecimal nz(BigDecimal v) {
        return v != null ? v : BigDecimal.ZERO;
    }

    private static BigDecimal money(BigDecimal v) {
        return nz(v).setScale(MONEY_SCALE, MONEY_ROUNDING);
    }

    private static void runAfterCommit(Runnable action) {
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            action.run();
            return;
        }
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                action.run();
            }
        });
    }
}
