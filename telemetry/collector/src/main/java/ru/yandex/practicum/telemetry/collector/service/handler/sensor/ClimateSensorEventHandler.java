package ru.yandex.practicum.telemetry.collector.service.handler.sensor;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.kafka.telemetry.event.ClimateSensorAvro;
import ru.yandex.practicum.kafka.telemetry.event.SensorEventAvro;
import ru.yandex.practicum.telemetry.collector.model.ClimateSensorEvent;
import ru.yandex.practicum.telemetry.collector.model.SensorEvent;
import ru.yandex.practicum.telemetry.collector.model.SensorEventType;
import ru.yandex.practicum.telemetry.collector.service.KafkaEventProducer;

@Component
public class ClimateSensorEventHandler extends BaseSensorEventHandler {

    public ClimateSensorEventHandler(KafkaEventProducer producer) {
        super(producer);
    }

    @Override
    protected SensorEventAvro mapToAvro(SensorEvent event) {
        ClimateSensorEvent record = (ClimateSensorEvent) event;

        ClimateSensorAvro payload = ClimateSensorAvro.newBuilder()
                .setTemperatureC(record.getTemperatureC())
                .setCo2Level(record.getCo2Level())
                .setHumidity(record.getHumidity())
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
        return SensorEventType.CLIMATE_SENSOR_EVENT;
    }
}

