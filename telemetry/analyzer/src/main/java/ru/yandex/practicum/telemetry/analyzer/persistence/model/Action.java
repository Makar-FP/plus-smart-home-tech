package ru.yandex.practicum.telemetry.analyzer.persistence.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.yandex.practicum.kafka.telemetry.event.ActionTypeAvro;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "actions")
public class Action {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ActionTypeAvro type;

    @Column(nullable = false)
    private Integer value;
}

