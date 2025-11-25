package ru.yandex.practicum.telemetry.aggregator.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.kafka.telemetry.event.SensorEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.SensorStateAvro;
import ru.yandex.practicum.kafka.telemetry.event.SensorsSnapshotAvro;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
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
            SensorsSnapshotAvro newSnapshot = addSnapshot(event);
            snapshots.put(hubId, newSnapshot);
            log.debug("Created new snapshot for hubId={}: {}", hubId, newSnapshot);
            return Optional.of(newSnapshot);
        }

        Map<String, SensorStateAvro> sensorsState = snapshot.getSensorsState();
        SensorStateAvro oldState = sensorsState.get(sensorId);

        if (oldState == null) {
            SensorStateAvro newState = toState(event);
            snapshot.setTimestamp(event.getTimestamp());
            sensorsState.put(sensorId, newState);
            log.debug("Added new sensor state for hubId={}, sensorId={}: {}", hubId, sensorId, newState);
            return Optional.of(snapshot);
        }

        log.debug("Existing state for hubId={}, sensorId={}: {}", hubId, sensorId, oldState);
        log.debug("Comparing timestamps: old={} vs new={}", oldState.getTimestamp(), event.getTimestamp());

        boolean isNewer = event.getTimestamp().isAfter(oldState.getTimestamp());
        boolean dataChanged = !Objects.equals(oldState.getData(), event.getPayload());

        log.debug("Data compare for sensorId={}: old={}, new={}, isNewer={}, dataChanged={}",
                sensorId, oldState.getData(), event.getPayload(), isNewer, dataChanged);

        if (!isNewer || !dataChanged) {
            log.debug("Snapshot not updated for hubId={}, sensorId={} (no change detected)", hubId, sensorId);
            return Optional.empty();
        }

        SensorStateAvro updatedState = toState(event);
        snapshot.setTimestamp(event.getTimestamp());
        sensorsState.put(sensorId, updatedState);

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
