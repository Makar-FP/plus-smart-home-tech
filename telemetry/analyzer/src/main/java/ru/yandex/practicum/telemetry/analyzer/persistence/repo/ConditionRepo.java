package ru.yandex.practicum.telemetry.analyzer.persistence.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.yandex.practicum.telemetry.analyzer.persistence.model.Condition;

public interface ConditionRepo extends JpaRepository<Condition, Long> {
}

