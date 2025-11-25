package ru.yandex.practicum.telemetry.aggregator.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.kafka.telemetry.event.SensorEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.SensorStateAvro;
import ru.yandex.practicum.kafka.telemetry.event.SensorsSnapshotAvro;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class InMemorySensorEvent {

    private final Map<String, SensorsSnapshotAvro> snapshots;

    public Optional<SensorsSnapshotAvro> updateState(SensorEventAvro event) {
        String hubId = event.getHubId();
        String sensorId = event.getId();

        SensorsSnapshotAvro snapshot = snapshots.get(hubId);
        if (snapshot == null) {
            SensorsSnapshotAvro created = addSnapshot(event);
            return Optional.of(created);
        }

        SensorStateAvro newState = SensorStateAvro.newBuilder()
                .setTimestamp(event.getTimestamp())
                .setData(event.getPayload())
                .build();

        snapshot.setTimestamp(event.getTimestamp());
        snapshot.getSensorsState().put(sensorId, newState);

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
