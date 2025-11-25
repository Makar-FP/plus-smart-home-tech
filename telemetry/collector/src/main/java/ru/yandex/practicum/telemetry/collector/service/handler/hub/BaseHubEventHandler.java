package ru.yandex.practicum.telemetry.collector.service.handler.hub;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ru.yandex.practicum.kafka.telemetry.event.HubEventAvro;
import ru.yandex.practicum.telemetry.collector.model.HubEvent;
import ru.yandex.practicum.telemetry.collector.service.KafkaEventProducer;
import ru.yandex.practicum.telemetry.collector.service.handler.HubEventHandler;

@Slf4j
@RequiredArgsConstructor
public abstract class BaseHubEventHandler implements HubEventHandler {

    protected final KafkaEventProducer producer;

    protected abstract HubEventAvro mapToAvro(HubEvent event);

    @Override
    public void handle(HubEvent event) {
        HubEventAvro record = mapToAvro(event);
        producer.sendHubEventToKafka(record);
    }
}

