package ru.yandex.practicum.telemetry.collector.service.handler.hub;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.kafka.telemetry.event.DeviceAddedEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.DeviceTypeAvro;
import ru.yandex.practicum.kafka.telemetry.event.HubEventAvro;
import ru.yandex.practicum.telemetry.collector.model.DeviceAddedEvent;
import ru.yandex.practicum.telemetry.collector.model.HubEvent;
import ru.yandex.practicum.telemetry.collector.model.HubEventType;
import ru.yandex.practicum.telemetry.collector.service.KafkaEventProducer;

@Component
public class DeviceHubAddedEventHandler extends BaseHubEventHandler {

    public DeviceHubAddedEventHandler(KafkaEventProducer producer) {
        super(producer);
    }

    @Override
    protected HubEventAvro mapToAvro(HubEvent event) {
        DeviceAddedEvent record = (DeviceAddedEvent) event;

        DeviceAddedEventAvro payload = DeviceAddedEventAvro.newBuilder()
                .setId(record.getId())
                .setType(DeviceTypeAvro.valueOf(record.getDeviceType().name()))
                .build();

        return HubEventAvro.newBuilder()
                .setHubId(record.getHubId())
                .setTimestamp(record.getTimestamp())
                .setPayload(payload)
                .build();
    }

    @Override
    public HubEventType getMessageType() {
        return HubEventType.DEVICE_ADDED;
    }
}

