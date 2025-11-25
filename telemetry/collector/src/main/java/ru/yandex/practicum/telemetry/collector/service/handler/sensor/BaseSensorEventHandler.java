package ru.yandex.practicum.telemetry.collector.service.handler.sensor;

import lombok.extern.slf4j.Slf4j;
import ru.yandex.practicum.grpc.telemetry.event.SensorEventProto;
import ru.yandex.practicum.kafka.telemetry.event.SensorEventAvro;
import ru.yandex.practicum.telemetry.collector.service.KafkaEventProducer;
import ru.yandex.practicum.telemetry.collector.service.handler.SensorEventHandler;

@Slf4j
public abstract class BaseSensorEventHandler implements SensorEventHandler {

    protected final KafkaEventProducer producer;

    protected BaseSensorEventHandler(KafkaEventProducer producer) {
        this.producer = producer;
    }

    protected abstract SensorEventAvro mapToAvro(SensorEventProto event);

    @Override
    public void handle(SensorEventProto event) {
        SensorEventAvro record = mapToAvro(event);
        if (record == null) {
            log.warn("Handler {} produced null Avro record for sensor event: {}",
                    getClass().getSimpleName(), event);
            return;
        }
        producer.sendSensorEventToKafka(record);
    }
}