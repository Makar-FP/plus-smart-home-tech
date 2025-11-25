package ru.yandex.practicum.telemetry.collector.model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString(callSuper = true)
public class Action {

    private String sensorId;

    private ActionType type;

    private int value;

}
