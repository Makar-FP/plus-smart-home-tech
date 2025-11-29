package ru.yandex.practicum.telemetry.analyzer.persistence.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.yandex.practicum.kafka.telemetry.event.ConditionOperationAvro;
import ru.yandex.practicum.kafka.telemetry.event.ConditionTypeAvro;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "conditions")
public class Condition {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ConditionTypeAvro type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ConditionOperationAvro operation;

    @Column(nullable = false)
    private Integer value;
}

