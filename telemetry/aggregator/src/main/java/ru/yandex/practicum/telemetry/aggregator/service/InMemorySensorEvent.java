package ru.yandex.practicum.telemetry.aggregator.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.kafka.telemetry.event.SensorEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.SensorStateAvro;
import ru.yandex.practicum.kafka.telemetry.event.SensorsSnapshotAvro;

import java.util.Map;
import java.util.HashMap;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class InMemorySensorEvent {

    private final Map<String, SensorsSnapshotAvro> snapshots = new ConcurrentHashMap<>();

    public Optional<SensorsSnapshotAvro> updateState(SensorEventAvro event) {
        String hubId = event.getHubId();
        String sensorId = event.getId();

        SensorsSnapshotAvro snapshot = snapshots.get(hubId);
        if (snapshot == null) {
            SensorsSnapshotAvro created = addSnapshot(event);
            return Optional.of(created);
        }

        Map<String, SensorStateAvro> sensorsState = snapshot.getSensorsState();
        SensorStateAvro oldState = sensorsState.get(sensorId);

        if (oldState == null) {
            SensorStateAvro newState = buildState(event);
            snapshot.setTimestamp(event.getTimestamp());
            sensorsState.put(sensorId, newState);
            return Optional.of(snapshot);
        }

        if (oldState.getTimestamp().isAfter(event.getTimestamp())) {
            return Optional.empty();
        }

        String oldDataStr = oldState.getData().toString();
        String newDataStr = event.getPayload().toString();
        if (oldDataStr.equals(newDataStr)) {
            return Optional.empty();
        }

        SensorStateAvro newState = buildState(event);
        snapshot.setTimestamp(event.getTimestamp());
        sensorsState.put(sensorId, newState);
        return Optional.of(snapshot);
    }

    private SensorStateAvro buildState(SensorEventAvro event) {
        return SensorStateAvro.newBuilder()
                .setTimestamp(event.getTimestamp())
                .setData(event.getPayload())
                .build();
    }

    private SensorsSnapshotAvro addSnapshot(SensorEventAvro record) {
        String hubId = record.getHubId();
        String sensorId = record.getId();

        Map<String, SensorStateAvro> sensorsState = new HashMap<>();
        sensorsState.put(sensorId, buildState(record));

        SensorsSnapshotAvro snapshot = SensorsSnapshotAvro.newBuilder()
                .setTimestamp(record.getTimestamp())
                .setHubId(hubId)
                .setSensorsState(sensorsState)
                .build();

        snapshots.put(hubId, snapshot);
        return snapshot;
    }
}
