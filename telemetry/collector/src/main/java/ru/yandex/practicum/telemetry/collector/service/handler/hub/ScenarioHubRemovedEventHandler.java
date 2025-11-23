package ru.yandex.practicum.telemetry.collector.service.handler.hub;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.kafka.telemetry.event.HubEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.ScenarioRemovedEventAvro;
import ru.yandex.practicum.telemetry.collector.model.HubEvent;
import ru.yandex.practicum.telemetry.collector.model.HubEventType;
import ru.yandex.practicum.telemetry.collector.model.ScenarioRemovedEvent;
import ru.yandex.practicum.telemetry.collector.service.KafkaEventProducer;

@Component
public class ScenarioHubRemovedEventHandler extends BaseHubEventHandler {

    public ScenarioHubRemovedEventHandler(KafkaEventProducer producer) {
        super(producer);
    }

    @Override
    protected HubEventAvro mapToAvro(HubEvent event) {
        ScenarioRemovedEvent record = (ScenarioRemovedEvent) event;

        ScenarioRemovedEventAvro payload = ScenarioRemovedEventAvro.newBuilder()
                .setName(record.getName())
                .build();

        return HubEventAvro.newBuilder()
                .setHubId(record.getHubId())
                .setTimestamp(record.getTimestamp())
                .setPayload(payload)
                .build();
    }

    @Override
    public HubEventType getMessageType() {
        return HubEventType.SCENARIO_REMOVED;
    }
}

