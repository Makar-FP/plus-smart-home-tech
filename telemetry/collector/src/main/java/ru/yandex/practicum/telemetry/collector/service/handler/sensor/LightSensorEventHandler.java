package ru.yandex.practicum.telemetry.collector.service.handler.sensor;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.kafka.telemetry.event.LightSensorAvro;
import ru.yandex.practicum.kafka.telemetry.event.SensorEventAvro;
import ru.yandex.practicum.telemetry.collector.model.LightSensorEvent;
import ru.yandex.practicum.telemetry.collector.model.SensorEvent;
import ru.yandex.practicum.telemetry.collector.model.SensorEventType;
import ru.yandex.practicum.telemetry.collector.service.KafkaEventProducer;

@Component
public class LightSensorEventHandler extends BaseSensorEventHandler {

    public LightSensorEventHandler(KafkaEventProducer producer) {
        super(producer);
    }

    @Override
    protected SensorEventAvro mapToAvro(SensorEvent event) {
        LightSensorEvent record = (LightSensorEvent) event;

        LightSensorAvro payload = LightSensorAvro.newBuilder()
                .setLinkQuality(record.getLinkQuality())
                .setLuminosity(record.getLuminosity())
                .build();

        return SensorEventAvro.newBuilder()
                .setId(record.getId())
                .setHubId(record.getHubId())
                .setTimestamp(record.getTimestamp())
                .setPayload(payload)
                .build();
    }

    @Override
    public SensorEventType getMessageType() {
        return SensorEventType.LIGHT_SENSOR_EVENT;
    }
}

