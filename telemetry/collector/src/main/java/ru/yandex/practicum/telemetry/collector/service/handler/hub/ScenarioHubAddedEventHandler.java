package ru.yandex.practicum.telemetry.collector.service.handler.hub;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.kafka.telemetry.event.*;
import ru.yandex.practicum.telemetry.collector.model.HubEvent;
import ru.yandex.practicum.telemetry.collector.model.HubEventType;
import ru.yandex.practicum.telemetry.collector.model.ScenarioAddedEvent;
import ru.yandex.practicum.telemetry.collector.service.KafkaEventProducer;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class ScenarioHubAddedEventHandler extends BaseHubEventHandler {

    public ScenarioHubAddedEventHandler(KafkaEventProducer producer) {
        super(producer);
    }

    @Override
    protected HubEventAvro mapToAvro(HubEvent event) {
        ScenarioAddedEvent record = (ScenarioAddedEvent) event;

        List<ScenarioConditionAvro> conditions = record.getConditions()
                .stream()
                .map(condition -> ScenarioConditionAvro.newBuilder()
                        .setSensorId(condition.getSensorId())
                        .setType(ConditionTypeAvro.valueOf(condition.getType().name()))
                        .setOperation(ConditionOperationAvro.valueOf(condition.getOperation().name()))
                        .setValue(condition.getValue())
                        .build()
                )
                .collect(Collectors.toList());

        List<DeviceActionAvro> actions = record.getActions()
                .stream()
                .map(action -> DeviceActionAvro.newBuilder()
                        .setSensorId(action.getSensorId())
                        .setType(ActionTypeAvro.valueOf(action.getType().name()))
                        .setValue(action.getValue())
                        .build()
                )
                .collect(Collectors.toList());

        ScenarioAddedEventAvro payload = ScenarioAddedEventAvro.newBuilder()
                .setName(record.getName())
                .setConditions(conditions)
                .setActions(actions)
                .build();

        return HubEventAvro.newBuilder()
                .setHubId(record.getHubId())
                .setTimestamp(record.getTimestamp())
                .setPayload(payload)
                .build();
    }

    @Override
    public HubEventType getMessageType() {
        return HubEventType.SCENARIO_ADDED;
    }
}

