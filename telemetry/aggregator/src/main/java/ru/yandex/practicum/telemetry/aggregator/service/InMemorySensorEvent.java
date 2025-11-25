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
            snapshot = createSnapshot(event);
            snapshots.put(hubId, snapshot);
            log.debug("Created new snapshot for hubId={}: {}", hubId, snapshot);
            return Optional.of(snapshot);
        }

        Map<String, SensorStateAvro> sensorsState = snapshot.getSensorsState();
        SensorStateAvro oldState = sensorsState.get(sensorId);
        SensorStateAvro newState = toState(event);

        if (oldState == null) {
            snapshot.setTimestamp(event.getTimestamp());
            sensorsState.put(sensorId, newState);
            log.debug("Added new sensor to snapshot for hubId={}, sensorId={}: {}",
                    hubId, sensorId, snapshot);
            return Optional.of(snapshot);
        }

        boolean isNewer = event.getTimestamp().isAfter(oldState.getTimestamp());
        boolean dataChanged = !Objects.equals(oldState.getData(), event.getPayload());

        if (!isNewer || !dataChanged) {
            log.debug("Event ignored for hubId={}, sensorId={} (isNewer={}, dataChanged={})",
                    hubId, sensorId, isNewer, dataChanged);
            return Optional.empty();
        }

        snapshot.setTimestamp(event.getTimestamp());
        sensorsState.put(sensorId, newState);

        log.debug("Snapshot updated for hubId={}, sensorId={}: {}", hubId, sensorId, snapshot);
        return Optional.of(snapshot);
    }

    private SensorsSnapshotAvro createSnapshot(SensorEventAvro event) {
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
