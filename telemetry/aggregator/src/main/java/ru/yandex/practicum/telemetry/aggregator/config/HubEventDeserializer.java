package ru.yandex.practicum.telemetry.aggregator.config;

import ru.yandex.practicum.kafka.telemetry.event.HubEventAvro;

public class HubEventDeserializer extends BaseAvroDeserializer<HubEventAvro> {
    public HubEventDeserializer() {

        super(HubEventAvro.getClassSchema());
    }
}
