package ru.yandex.practicum.commerce.delivery.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.commerce.delivery.mapper.DeliveryMapper;
import ru.yandex.practicum.commerce.delivery.model.Delivery;
import ru.yandex.practicum.commerce.delivery.model.DeliveryAddress;
import ru.yandex.practicum.commerce.delivery.repo.DeliveryRepository;
import ru.yandex.practicum.commerce.interactionapi.client.OrderClient;
import ru.yandex.practicum.commerce.interactionapi.client.WarehouseClient;
import ru.yandex.practicum.commerce.interactionapi.dto.delivery.DeliveryDto;
import ru.yandex.practicum.commerce.interactionapi.dto.delivery.DeliveryState;
import ru.yandex.practicum.commerce.interactionapi.dto.order.OrderDto;
import ru.yandex.practicum.commerce.interactionapi.dto.warehouse.ShippedToDeliveryRequest;
import ru.yandex.practicum.commerce.interactionapi.exception.NoDeliveryFoundException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DeliveryServiceImpl implements DeliveryService {

    private static final String ADDRESS_2_MARKER = "ADDRESS_2";
    private static final BigDecimal ADDRESS_2_MULTIPLIER = BigDecimal.valueOf(2);

    private static final BigDecimal BASE_PRICE = new BigDecimal("5.00");
    private static final BigDecimal OTHER_STREET_RATE = new BigDecimal("1.20");

    private static final BigDecimal FRAGILE_RATE = new BigDecimal("0.20");
    private static final BigDecimal WEIGHT_RATE = new BigDecimal("0.30");
    private static final BigDecimal VOLUME_RATE = new BigDecimal("0.20");

    private final DeliveryRepository deliveryRepository;
    private final DeliveryMapper mapper;
    private final OrderClient orderClient;
    private final WarehouseClient warehouseClient;

    @Override
    @Transactional
    public DeliveryDto planDelivery(DeliveryDto deliveryDto) {
        Delivery delivery = mapper.toDelivery(deliveryDto);
        Delivery saved = deliveryRepository.save(delivery);

        warehouseClient.shippedToDelivery(
                new ShippedToDeliveryRequest(saved.getDeliveryId(), saved.getOrderId())
        );

        return mapper.toDeliveryDto(saved);
    }

    @Override
    @Transactional
    public void deliverySuccessful(UUID orderId) {
        updateState(orderId, DeliveryState.DELIVERED);
        orderClient.delivery(orderId);
    }

    @Override
    @Transactional
    public void deliveryPicked(UUID orderId) {
        updateState(orderId, DeliveryState.IN_PROGRESS);
        orderClient.complete(orderId);
    }

    @Override
    @Transactional
    public void deliveryFailed(UUID orderId) {
        updateState(orderId, DeliveryState.FAILED);
        orderClient.deliveryFailed(orderId);
    }

    @Override
    @Transactional
    public BigDecimal deliveryCost(OrderDto order) {
        Delivery delivery = getByOrderId(order.getOrderId());

        DeliveryAddress from = delivery.getDeliveryFromAddress();
        DeliveryAddress to = delivery.getDeliveryToAddress();

        BigDecimal addressMultiplier = addressMultiplier(from);
        BigDecimal price = BASE_PRICE.multiply(addressMultiplier);

        if (delivery.isFragile()) {
            price = price.add(price.multiply(FRAGILE_RATE));
        }

        price = price.add(BigDecimal.valueOf(delivery.getDeliveryWeight()).multiply(WEIGHT_RATE));
        price = price.add(BigDecimal.valueOf(delivery.getDeliveryVolume()).multiply(VOLUME_RATE));

        if (from != null && to != null && from.getStreet() != null && to.getStreet() != null
                && !from.getStreet().equalsIgnoreCase(to.getStreet())) {
            price = price.multiply(OTHER_STREET_RATE);
        }

        return price.setScale(2, RoundingMode.HALF_UP);
    }

    private void updateState(UUID orderId, DeliveryState state) {
        Delivery delivery = getByOrderId(orderId);
        delivery.setDeliveryState(state);
        deliveryRepository.save(delivery);
    }

    private Delivery getByOrderId(UUID orderId) {
        return deliveryRepository.findByOrderId(orderId)
                .orElseThrow(NoDeliveryFoundException::new);
    }

    private BigDecimal addressMultiplier(DeliveryAddress from) {
        if (from == null) return BigDecimal.ONE;

        return String.valueOf(from).contains(ADDRESS_2_MARKER)
                ? ADDRESS_2_MULTIPLIER
                : BigDecimal.ONE;
    }
}
