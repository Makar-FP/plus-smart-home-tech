package ru.yandex.practicum.telemetry.analyzer.persistence.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.yandex.practicum.telemetry.analyzer.persistence.model.Sensor;

import java.util.Collection;
import java.util.Optional;

public interface SensorRepo extends JpaRepository<Sensor, String> {

    boolean existsByIdInAndHubId(Collection<String> ids, String hubId);

    Optional<Sensor> findByIdAndHubId(String id, String hubId);
}

