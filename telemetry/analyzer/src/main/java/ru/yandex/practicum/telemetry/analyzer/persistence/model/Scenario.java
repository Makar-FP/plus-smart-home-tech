package ru.yandex.practicum.telemetry.analyzer.persistence.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(
        name = "scenarios",
        uniqueConstraints = @UniqueConstraint(columnNames = {"hub_id", "name"})
)
public class Scenario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "hub_id", nullable = false)
    private String hubId;

    @Column(nullable = false)
    private String name;

    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
    @MapKeyColumn(name = "sensor_id")
    @JoinTable(
            name = "scenario_conditions",
            joinColumns = @JoinColumn(name = "scenario_id", referencedColumnName = "id"),
            inverseJoinColumns = @JoinColumn(name = "condition_id", referencedColumnName = "id")
    )
    private Map<String, Condition> conditions = new HashMap<>();

    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
    @MapKeyColumn(name = "sensor_id")
    @JoinTable(
            name = "scenario_actions",
            joinColumns = @JoinColumn(name = "scenario_id", referencedColumnName = "id"),
            inverseJoinColumns = @JoinColumn(name = "action_id", referencedColumnName = "id")
    )
    private Map<String, Action> actions = new HashMap<>();
}

