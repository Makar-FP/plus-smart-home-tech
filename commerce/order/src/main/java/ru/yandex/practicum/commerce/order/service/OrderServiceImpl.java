package ru.yandex.practicum.commerce.order.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.commerce.interactionapi.client.DeliveryClient;
import ru.yandex.practicum.commerce.interactionapi.client.PaymentClient;
import ru.yandex.practicum.commerce.interactionapi.client.WarehouseClient;
import ru.yandex.practicum.commerce.interactionapi.dto.common.AddressDto;
import ru.yandex.practicum.commerce.interactionapi.dto.common.BookedProductsDto;
import ru.yandex.practicum.commerce.interactionapi.dto.delivery.DeliveryDto;
import ru.yandex.practicum.commerce.interactionapi.dto.order.CreateNewOrderRequest;
import ru.yandex.practicum.commerce.interactionapi.dto.order.OrderDto;
import ru.yandex.practicum.commerce.interactionapi.dto.order.OrderState;
import ru.yandex.practicum.commerce.interactionapi.dto.order.ProductReturnRequest;
import ru.yandex.practicum.commerce.interactionapi.dto.payment.PaymentDto;
import ru.yandex.practicum.commerce.interactionapi.dto.warehouse.AssemblyProductsForOrderRequest;
import ru.yandex.practicum.commerce.interactionapi.exception.NoOrderFoundException;
import ru.yandex.practicum.commerce.interactionapi.exception.NotAuthorizedUserException;
import ru.yandex.practicum.commerce.order.mapper.OrderMapper;
import ru.yandex.practicum.commerce.order.model.Order;
import ru.yandex.practicum.commerce.order.model.OrderProduct;
import ru.yandex.practicum.commerce.order.repo.OrderRepository;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final OrderMapper mapper;
    private final PaymentClient paymentClient;
    private final DeliveryClient deliveryClient;
    private final WarehouseClient warehouseClient;

    @Override
    @Transactional(Transactional.TxType.SUPPORTS)
    public List<OrderDto> getClientOrders(String username) throws NotAuthorizedUserException {
        if (username == null || username.isBlank()) {
            throw new NotAuthorizedUserException(username);
        }

        return orderRepository.findByUsername(username)
                .orElseGet(List::of)
                .stream()
                .map(mapper::toOrderDto)
                .toList();
    }

    @Override
    @Transactional
    public OrderDto createNewOrder(CreateNewOrderRequest request) {
        Objects.requireNonNull(request, "request must not be null");

        Order order = mapper.toOrder(request);

        if (!tryAssembly(order, request)) {
            return mapper.toOrderDto(orderRepository.save(order));
        }

        if (!tryCalcProductsCost(order)) {
            return mapper.toOrderDto(orderRepository.save(order));
        }

        if (!tryPlanDelivery(order)) {
            return mapper.toOrderDto(orderRepository.save(order));
        }

        Order saved = orderRepository.save(order);
        return mapper.toOrderDto(saved);
    }

    @Override
    @Transactional
    public OrderDto productReturn(ProductReturnRequest request) {
        Objects.requireNonNull(request, "request must not be null");
        Objects.requireNonNull(request.getOrderId(), "orderId must not be null");
        Objects.requireNonNull(request.getProducts(), "products must not be null");

        Order order = orderRepository.findById(request.getOrderId())
                .orElseThrow(() -> new NoOrderFoundException(request.getOrderId()));

        Map<UUID, Long> returns = request.getProducts();

        List<OrderProduct> updated = new ArrayList<>();
        for (OrderProduct p : order.getProducts()) {
            if (p == null) continue;

            long toReturn = returns.getOrDefault(p.getProductId(), 0L);
            if (toReturn < 0) {
                throw new IllegalArgumentException("Return quantity must be >= 0 for productId=" + p.getProductId());
            }

            long newQty = p.getQuantity() - toReturn;
            if (newQty < 0) {
                throw new IllegalArgumentException("Return quantity exceeds owned quantity for productId=" + p.getProductId());
            }
            if (newQty > 0) {
                p.setQuantity(newQty);
                updated.add(p);
            }
        }

        order.setProducts(updated);
        order.setState(OrderState.PRODUCT_RETURNED);

        Order saved = orderRepository.save(order);
        return mapper.toOrderDto(saved);
    }

    @Override
    @Transactional
    public OrderDto updateOrderStatus(UUID orderId, OrderState state) {
        Objects.requireNonNull(orderId, "orderId must not be null");
        Objects.requireNonNull(state, "state must not be null");

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new NoOrderFoundException(orderId));

        if (state == OrderState.ON_PAYMENT) {
            PaymentDto paymentDto = paymentClient.payment(mapper.toOrderDto(order));
            order.setPaymentId(paymentDto.getPaymentId());
        }

        order.setState(state);

        Order saved = orderRepository.save(order);
        return mapper.toOrderDto(saved);
    }

    @Override
    @Transactional
    public OrderDto calculateTotalCost(UUID orderId) {
        Objects.requireNonNull(orderId, "orderId must not be null");

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new NoOrderFoundException(orderId));

        BigDecimal totalCost = paymentClient.getTotalCost(mapper.toOrderDto(order));
        order.setTotalPrice(totalCost);

        Order saved = orderRepository.save(order);
        return mapper.toOrderDto(saved);
    }

    @Override
    @Transactional
    public OrderDto calculateDeliveryCost(UUID orderId) {
        Objects.requireNonNull(orderId, "orderId must not be null");

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new NoOrderFoundException(orderId));

        BigDecimal deliveryCost = deliveryClient.deliveryCost(mapper.toOrderDto(order));
        order.setDeliverPrice(deliveryCost);

        Order saved = orderRepository.save(order);
        return mapper.toOrderDto(saved);
    }

    private boolean tryAssembly(Order order, CreateNewOrderRequest request) {
        try {
            BookedProductsDto booked = warehouseClient.assemblyProductsForOrder(
                    new AssemblyProductsForOrderRequest(order.getOrderId(), request.getShoppingCart().getProducts())
            );
            order.setDeliveryVolume(booked.getDeliveryVolume());
            order.setDeliveryWeight(booked.getDeliveryWeight());
            order.setFragile(booked.isFragile());
            return true;
        } catch (Exception e) {
            order.setState(OrderState.ASSEMBLY_FAILED);
            return false;
        }
    }

    private boolean tryCalcProductsCost(Order order) {
        try {
            BigDecimal productsPrice = paymentClient.productCost(mapper.toOrderDto(order));
            order.setProductPrice(productsPrice);
            return true;
        } catch (Exception e) {
            order.setState(OrderState.PAYMENT_FAILED);
            return false;
        }
    }

    private boolean tryPlanDelivery(Order order) {
        try {
            AddressDto fromAddress = warehouseClient.getWarehouseAddress();
            DeliveryDto deliveryDto = mapper.toDeliveryDto(order, fromAddress);
            DeliveryDto planned = deliveryClient.planDelivery(deliveryDto);
            order.setDeliveryId(planned.getDeliveryId());
            return true;
        } catch (Exception e) {
            order.setState(OrderState.DELIVERY_FAILED);
            return false;
        }
    }
}
