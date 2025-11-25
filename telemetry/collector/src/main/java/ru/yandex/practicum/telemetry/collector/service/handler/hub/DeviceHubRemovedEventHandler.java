package ru.yandex.practicum.telemetry.collector.service.handler.hub;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.kafka.telemetry.event.DeviceRemovedEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.HubEventAvro;
import ru.yandex.practicum.telemetry.collector.model.DeviceRemovedEvent;
import ru.yandex.practicum.telemetry.collector.model.HubEvent;
import ru.yandex.practicum.telemetry.collector.model.HubEventType;
import ru.yandex.practicum.telemetry.collector.service.KafkaEventProducer;

@Component
public class DeviceHubRemovedEventHandler extends BaseHubEventHandler {

    public DeviceHubRemovedEventHandler(KafkaEventProducer producer) {
        super(producer);
    }

    @Override
    protected HubEventAvro mapToAvro(HubEvent event) {
        DeviceRemovedEvent record = (DeviceRemovedEvent) event;

        DeviceRemovedEventAvro payload = DeviceRemovedEventAvro.newBuilder()
                .setId(record.getId())
                .build();

        return HubEventAvro.newBuilder()
                .setHubId(record.getHubId())
                .setTimestamp(record.getTimestamp())
                .setPayload(payload)
                .build();
    }

    @Override
    public HubEventType getMessageType() {
        return HubEventType.DEVICE_REMOVED;
    }
}
