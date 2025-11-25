package ru.yandex.practicum.telemetry.aggregator.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.kafka.telemetry.event.SensorEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.SensorStateAvro;
import ru.yandex.practicum.kafka.telemetry.event.SensorsSnapshotAvro;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class InMemorySensorEvent {

    private final Map<String, SensorsSnapshotAvro> snapshots = new ConcurrentHashMap<>();

    public Optional<SensorsSnapshotAvro> updateState(SensorEventAvro event) {
        log.debug("Snapshots map: {}", snapshots);
        String hubId = event.getHubId();
        String sensorId = event.getId();

        SensorsSnapshotAvro snapshot = snapshots.get(hubId);
        if (snapshot != null) {
            log.debug("Snapshot found for hubId={}", hubId);

            SensorStateAvro oldState = snapshot.getSensorsState().get(sensorId);
            if (oldState != null) {
                log.debug("Previous state found for sensorId={}: {}", sensorId, oldState);

                boolean timestampIsNewer =
                        event.getTimestamp().isAfter(oldState.getTimestamp());
                boolean dataEquals = Objects.equals(oldState.getData(), event.getPayload());

                if (!timestampIsNewer || dataEquals) {
                    log.debug("Previous state is newer or unchanged. Snapshot will not be updated.");
                    return Optional.empty();
                }

                SensorStateAvro newState = SensorStateAvro.newBuilder()
                        .setTimestamp(event.getTimestamp())
                        .setData(event.getPayload())
                        .build();

                snapshot.setTimestamp(event.getTimestamp());
                snapshot.getSensorsState().put(sensorId, newState);
                log.debug("Updated snapshot: {}", snapshot);

                return Optional.of(snapshot);
            } else {
                log.debug("No previous state for sensorId={}, adding new state", sensorId);
                SensorStateAvro newState = SensorStateAvro.newBuilder()
                        .setTimestamp(event.getTimestamp())
                        .setData(event.getPayload())
                        .build();

                snapshot.setTimestamp(event.getTimestamp());
                snapshot.getSensorsState().put(sensorId, newState);
                log.debug("Updated snapshot with new sensor state: {}", snapshot);

                return Optional.of(snapshot);
            }
        }

        log.debug("No snapshot for hubId={}, creating new", hubId);
        SensorsSnapshotAvro newSnapshot = addSnapshot(event);
        return Optional.of(newSnapshot);
    }

    private SensorsSnapshotAvro addSnapshot(SensorEventAvro record) {
        String hubId = record.getHubId();

        Map<String, SensorStateAvro> sensorsState = new HashMap<>();
        SensorStateAvro state = SensorStateAvro.newBuilder()
                .setTimestamp(record.getTimestamp())
                .setData(record.getPayload())
                .build();
        sensorsState.put(record.getId(), state);

        SensorsSnapshotAvro snapshot  = SensorsSnapshotAvro.newBuilder()
                .setTimestamp(record.getTimestamp())
                .setHubId(hubId)
                .setSensorsState(sensorsState)
                .build();

        snapshots.put(hubId, snapshot);
        log.debug("New snapshot created for hubId={}: {}", hubId, snapshot);
        return snapshot;
    }
}

