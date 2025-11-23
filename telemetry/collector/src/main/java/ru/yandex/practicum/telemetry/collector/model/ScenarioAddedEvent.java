package ru.yandex.practicum.telemetry.collector.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@Getter
@Setter
@ToString(callSuper = true)
@NotNull
public class ScenarioAddedEvent extends HubEvent {

    @NotBlank
    private String name;

    private List<Condition> conditions;

    private List<Action> actions;

    @Override
    public HubEventType getType() {
        return HubEventType.SCENARIO_ADDED;
    }
}
