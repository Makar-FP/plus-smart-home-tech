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

        log.debug("Updating snapshot for hubId={}, sensorId={}", hubId, sensorId);

        SensorsSnapshotAvro snapshot = snapshots.get(hubId);
        if (snapshot == null) {
            SensorsSnapshotAvro created = addSnapshot(event);
            log.debug("Created new snapshot for hubId={}: {}", hubId, created);
            return Optional.of(created);
        }

        SensorStateAvro newState = SensorStateAvro.newBuilder()
                .setTimestamp(event.getTimestamp())
                .setData(event.getPayload())
                .build();

        snapshot.setTimestamp(event.getTimestamp());
        snapshot.getSensorsState().put(sensorId, newState);

        log.debug("Updated snapshot for hubId={}, sensorId={}: {}", hubId, sensorId, snapshot);
        return Optional.of(snapshot);
    }

    private SensorsSnapshotAvro addSnapshot(SensorEventAvro record) {
        String hubId = record.getHubId();
        String sensorId = record.getId();

        Map<String, SensorStateAvro> sensorsState = new HashMap<>();
        SensorStateAvro state = SensorStateAvro.newBuilder()
                .setTimestamp(record.getTimestamp())
                .setData(record.getPayload())
                .build();
        sensorsState.put(sensorId, state);

        SensorsSnapshotAvro snapshot = SensorsSnapshotAvro.newBuilder()
                .setTimestamp(record.getTimestamp())
                .setHubId(hubId)
                .setSensorsState(sensorsState)
                .build();

        snapshots.put(hubId, snapshot);
        return snapshot;
    }
}
