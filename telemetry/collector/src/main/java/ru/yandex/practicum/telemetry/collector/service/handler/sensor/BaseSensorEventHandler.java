package ru.yandex.practicum.telemetry.collector.service.handler.sensor;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ru.yandex.practicum.kafka.telemetry.event.SensorEventAvro;
import ru.yandex.practicum.telemetry.collector.model.SensorEvent;
import ru.yandex.practicum.telemetry.collector.service.KafkaEventProducer;
import ru.yandex.practicum.telemetry.collector.service.handler.SensorEventHandler;

@Slf4j
@RequiredArgsConstructor
public abstract class BaseSensorEventHandler implements SensorEventHandler {

    protected final KafkaEventProducer producer;

    protected abstract SensorEventAvro mapToAvro(SensorEvent event);

    @Override
    public void handle(SensorEvent event) {
        SensorEventAvro record = mapToAvro(event);
        producer.sendSensorEventToKafka(record);
    }
}

