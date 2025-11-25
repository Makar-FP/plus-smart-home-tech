package ru.yandex.practicum.telemetry.aggregator.repo;

import ru.yandex.practicum.kafka.telemetry.event.SensorEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.SensorSnapshotAvro;

import java.util.Optional;

public interface SensorSnapshotRepository {

    Optional<SensorSnapshotAvro> updateSnapshot(SensorEventAvro event);

}

