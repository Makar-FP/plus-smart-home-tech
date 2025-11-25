package ru.yandex.practicum.telemetry.collector.model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString(callSuper = true)
public class Condition {

    private String sensorId;

    private ConditionType type;

    private OperationType operation;

    private int value;

}
