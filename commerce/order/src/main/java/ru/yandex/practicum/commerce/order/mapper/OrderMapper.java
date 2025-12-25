package ru.yandex.practicum.commerce.order.mapper;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.commerce.interactionapi.dto.common.AddressDto;
import ru.yandex.practicum.commerce.interactionapi.dto.delivery.DeliveryDto;
import ru.yandex.practicum.commerce.interactionapi.dto.delivery.DeliveryState;
import ru.yandex.practicum.commerce.interactionapi.dto.order.CreateNewOrderRequest;
import ru.yandex.practicum.commerce.interactionapi.dto.order.OrderDto;
import ru.yandex.practicum.commerce.interactionapi.dto.order.OrderState;
import ru.yandex.practicum.commerce.order.model.Order;
import ru.yandex.practicum.commerce.order.model.OrderAddress;
import ru.yandex.practicum.commerce.order.model.OrderProduct;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class OrderMapper {

    public Order toOrder(CreateNewOrderRequest request) {
        Objects.requireNonNull(request, "request must not be null");
        var cart = Objects.requireNonNull(request.getShoppingCart(), "shoppingCart must not be null");
        var deliveryAddress = Objects.requireNonNull(request.getDeliveryAddress(), "deliveryAddress must not be null");
        var cartProducts = Objects.requireNonNull(cart.getProducts(), "shoppingCart.products must not be null");

        Order order = new Order();
        order.setOrderId(UUID.randomUUID());
        order.setState(OrderState.NEW);
        order.setUsername(cart.getUsername());
        order.setShoppingCartId(cart.getShoppingCartId());
        order.setOrderAddress(toOrderAddress(deliveryAddress));
        order.setProducts(toOrderProducts(cartProducts));

        return order;
    }

    public OrderDto toOrderDto(Order order) {
        Objects.requireNonNull(order, "order must not be null");

        Map<UUID, Long> products = order.getProducts() == null
                ? Map.of()
                : order.getProducts().stream()
                .filter(Objects::nonNull)
                .collect(Collectors.toMap(
                        OrderProduct::getProductId,
                        OrderProduct::getQuantity,
                        Long::sum
                ));

        return new OrderDto(
                order.getOrderId(),
                order.getShoppingCartId(),
                products,
                order.getPaymentId(),
                order.getDeliveryId(),
                order.getState(),
                order.getDeliveryWeight(),
                order.getDeliveryVolume(),
                order.isFragile(),
                order.getTotalPrice(),
                order.getDeliverPrice(),
                order.getProductPrice()
        );
    }

    public DeliveryDto toDeliveryDto(Order order, AddressDto fromAddress) {
        Objects.requireNonNull(order, "order must not be null");
        Objects.requireNonNull(fromAddress, "fromAddress must not be null");
        Objects.requireNonNull(order.getOrderAddress(), "orderAddress must not be null");

        DeliveryDto deliveryDto = new DeliveryDto();
        deliveryDto.setDeliveryId(UUID.randomUUID());
        deliveryDto.setDeliveryState(DeliveryState.CREATED);
        deliveryDto.setOrderId(order.getOrderId());
        deliveryDto.setToAddress(toAddressDto(order.getOrderAddress()));
        deliveryDto.setFromAddress(fromAddress);

        return deliveryDto;
    }

    private OrderAddress toOrderAddress(AddressDto deliveryAddress) {
        OrderAddress address = new OrderAddress();
        address.setAddressId(UUID.randomUUID());
        address.setCountry(deliveryAddress.getCountry());
        address.setCity(deliveryAddress.getCity());
        address.setStreet(deliveryAddress.getStreet());
        address.setHouse(deliveryAddress.getHouse());
        address.setFlat(deliveryAddress.getFlat());
        return address;
    }

    private AddressDto toAddressDto(OrderAddress orderAddress) {
        AddressDto dto = new AddressDto();
        dto.setCountry(orderAddress.getCountry());
        dto.setCity(orderAddress.getCity());
        dto.setStreet(orderAddress.getStreet());
        dto.setHouse(orderAddress.getHouse());
        dto.setFlat(orderAddress.getFlat());
        return dto;
    }

    private List<OrderProduct> toOrderProducts(Map<UUID, Long> products) {
        List<OrderProduct> result = new ArrayList<>(products.size());

        for (var entry : products.entrySet()) {
            UUID productId = Objects.requireNonNull(entry.getKey(), "productId must not be null");
            Long quantity = Objects.requireNonNull(entry.getValue(), "quantity must not be null");
            if (quantity <= 0) {
                throw new IllegalArgumentException("quantity must be > 0 for productId=" + productId);
            }

            OrderProduct product = new OrderProduct();
            product.setId(UUID.randomUUID());
            product.setProductId(productId);
            product.setQuantity(quantity);
            result.add(product);
        }

        return result;
    }
}
