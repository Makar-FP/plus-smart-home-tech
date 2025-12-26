package ru.yandex.practicum.commerce.delivery.mapper;

import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.commerce.delivery.model.Delivery;
import ru.yandex.practicum.commerce.delivery.model.DeliveryAddress;
import ru.yandex.practicum.commerce.interactionapi.dto.common.AddressDto;
import ru.yandex.practicum.commerce.interactionapi.dto.delivery.DeliveryDto;

@Component
@RequiredArgsConstructor
public class DeliveryMapper {

    private final ModelMapper modelMapper;

    public Delivery toDelivery(DeliveryDto dto) {
        if (dto == null) return null;

        Delivery delivery = new Delivery();
        delivery.setDeliveryId(dto.getDeliveryId());
        delivery.setDeliveryState(dto.getDeliveryState());
        delivery.setOrderId(dto.getOrderId());

        delivery.setDeliveryFromAddress(map(dto.getFromAddress(), DeliveryAddress.class));
        delivery.setDeliveryToAddress(map(dto.getToAddress(), DeliveryAddress.class));

        return delivery;
    }

    public DeliveryDto toDeliveryDto(Delivery entity) {
        if (entity == null) return null;

        DeliveryDto dto = new DeliveryDto();
        dto.setDeliveryId(entity.getDeliveryId());
        dto.setDeliveryState(entity.getDeliveryState());
        dto.setOrderId(entity.getOrderId());

        dto.setFromAddress(map(entity.getDeliveryFromAddress(), AddressDto.class));
        dto.setToAddress(map(entity.getDeliveryToAddress(), AddressDto.class));

        return dto;
    }

    private <S, T> T map(S source, Class<T> targetType) {
        return source == null ? null : modelMapper.map(source, targetType);
    }
}
