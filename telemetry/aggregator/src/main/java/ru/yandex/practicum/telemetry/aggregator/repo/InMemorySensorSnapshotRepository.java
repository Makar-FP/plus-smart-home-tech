package ru.yandex.practicum.telemetry.aggregator.repo;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.kafka.telemetry.event.SensorEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.SensorSnapshotAvro;
import ru.yandex.practicum.kafka.telemetry.event.SensorStateAvro;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

@Slf4j
@Repository
@RequiredArgsConstructor
public class InMemorySensorSnapshotRepository implements SensorSnapshotRepository {

    private final Map<String, SensorSnapshotAvro> snapshots;

    @Override
    public Optional<SensorSnapshotAvro> updateSnapshot(SensorEventAvro event) {
        log.debug("Current snapshots: {}", snapshots);

        String hubId = event.getHubId();
        String sensorId = event.getId();

        SensorSnapshotAvro snapshot = snapshots.get(hubId);
        if (snapshot != null) {
            log.debug("Snapshot found for hubId={}", hubId);

            SensorStateAvro oldState = snapshot.getSensorsState().get(sensorId);
            if (oldState != null) {
                log.debug("Previous state found for sensorId={}: {}", sensorId, oldState);
                log.debug("Comparing timestamps: old={} vs new={}",
                        oldState.getTimestamp(), event.getTimestamp());

                boolean timestampIsNewer =
                        event.getTimestamp().isAfter(oldState.getTimestamp());
                boolean dataEquals = Objects.equals(oldState.getData(), event.getPayload());

                log.debug("Comparing state data for sensorId={}: old=\"{}\" vs new=\"{}\"",
                        sensorId, oldState.getData(), event.getPayload());

                if (!timestampIsNewer || dataEquals) {
                    log.debug("Previous state is newer or unchanged. Snapshot will not be updated.");
                    return Optional.empty();
                }

                log.debug("Updating existing state in snapshot");
                SensorStateAvro newState = SensorStateAvro.newBuilder()
                        .setTimestamp(event.getTimestamp())
                        .setData(event.getPayload())
                        .build();

                snapshot.setTimestamp(event.getTimestamp());
                snapshot.getSensorsState().put(sensorId, newState);

                log.debug("Updated snapshot: {}", snapshot);
                return Optional.of(snapshot);

            } else {
                log.debug("No previous state for sensorId={}, adding new state to snapshot", sensorId);
                SensorStateAvro newState = SensorStateAvro.newBuilder()
                        .setTimestamp(event.getTimestamp())
                        .setData(event.getPayload())
                        .build();

                snapshot.setTimestamp(event.getTimestamp());
                snapshot.getSensorsState().put(sensorId, newState);

                log.debug("Updated snapshot with new sensor state: {}", snapshot);
                return Optional.of(snapshot);
            }
        } else {
            log.debug("No snapshot found for hubId={}. Creating new snapshot.", hubId);
            return Optional.of(addSnapshot(event));
        }
    }

    private SensorSnapshotAvro addSnapshot(SensorEventAvro record) {
        String hubId = record.getHubId();

        Map<String, SensorStateAvro> sensorsState = new HashMap<>();
        SensorStateAvro state = SensorStateAvro.newBuilder()
                .setTimestamp(record.getTimestamp())
                .setData(record.getPayload())
                .build();

        sensorsState.put(record.getId(), state);

        SensorSnapshotAvro snapshot = SensorSnapshotAvro.newBuilder()
                .setTimestamp(record.getTimestamp())
                .setHubId(hubId)
                .setSensorsState(sensorsState)
                .build();

        snapshots.put(hubId, snapshot);
        log.debug("New snapshot created for hubId={}: {}", hubId, snapshot);
        return snapshot;
    }
}
