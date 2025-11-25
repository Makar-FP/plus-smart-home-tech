package ru.yandex.practicum.telemetry.aggregator.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.kafka.telemetry.event.SensorEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.SensorStateAvro;
import ru.yandex.practicum.kafka.telemetry.event.SensorsSnapshotAvro;

import java.util.HashMap;
import java.util.Map;
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
            snapshot = addSnapshot(event);
            snapshots.put(hubId, snapshot);
            log.debug("Created new snapshot for hubId={}: {}", hubId, snapshot);
            return Optional.of(snapshot);
        }

        Map<String, SensorStateAvro> sensorsState = snapshot.getSensorsState();
        SensorStateAvro newState = toState(event);

        snapshot.setTimestamp(event.getTimestamp());
        sensorsState.put(sensorId, newState);

        log.debug("Snapshot updated for hubId={}, sensorId={}: {}", hubId, sensorId, snapshot);
        return Optional.of(snapshot);
    }

    private SensorsSnapshotAvro addSnapshot(SensorEventAvro event) {
        String hubId = event.getHubId();
        String sensorId = event.getId();

        Map<String, SensorStateAvro> sensorsState = new HashMap<>();
        sensorsState.put(sensorId, toState(event));

        return SensorsSnapshotAvro.newBuilder()
                .setTimestamp(event.getTimestamp())
                .setHubId(hubId)
                .setSensorsState(sensorsState)
                .build();
    }

    private SensorStateAvro toState(SensorEventAvro event) {
        return SensorStateAvro.newBuilder()
                .setTimestamp(event.getTimestamp())
                .setData(event.getPayload())
                .build();
    }
}
