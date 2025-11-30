package ru.yandex.practicum.telemetry.analyzer.persistence.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.yandex.practicum.telemetry.analyzer.persistence.model.Scenario;

import java.util.List;
import java.util.Optional;

public interface ScenarioRepo extends JpaRepository<Scenario, Long> {

    List<Scenario> findByHubId(String hubId);

    Optional<Scenario> findByHubIdAndName(String hubId, String name);
}

