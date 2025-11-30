package ru.yandex.practicum.telemetry.analyzer.persistence.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.yandex.practicum.telemetry.analyzer.persistence.model.Action;

public interface ActionRepo extends JpaRepository<Action, Long> {
}

