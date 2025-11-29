package ru.yandex.practicum.telemetry.analyzer.service.logic;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.kafka.telemetry.event.*;
import ru.yandex.practicum.telemetry.analyzer.persistence.model.Action;
import ru.yandex.practicum.telemetry.analyzer.persistence.model.Condition;
import ru.yandex.practicum.telemetry.analyzer.persistence.model.Scenario;
import ru.yandex.practicum.telemetry.analyzer.persistence.model.Sensor;
import ru.yandex.practicum.telemetry.analyzer.persistence.repo.ActionRepo;
import ru.yandex.practicum.telemetry.analyzer.persistence.repo.ConditionRepo;
import ru.yandex.practicum.telemetry.analyzer.persistence.repo.ScenarioRepo;
import ru.yandex.practicum.telemetry.analyzer.persistence.repo.SensorRepo;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class HubHandlerService {

    private final SensorRepo sensorRepo;
    private final ConditionRepo conditionRepo;
    private final ActionRepo actionRepo;
    private final ScenarioRepo scenarioRepo;

    public void handleRecord(HubEventAvro record) {
        Object payload = record.getPayload();
        String hubId = record.getHubId();

        if (payload instanceof DeviceAddedEventAvro event) {
            handleDeviceAdded(hubId, event);
        } else if (payload instanceof DeviceRemovedEventAvro event) {
            handleDeviceRemoved(event);
        } else if (payload instanceof ScenarioAddedEventAvro event) {
            handleScenarioAdded(hubId, event);
        } else if (payload instanceof ScenarioRemovedEventAvro event) {
            handleScenarioRemoved(hubId, event);
        } else {
            log.warn("Unknown HubEvent payload type: {}", payload.getClass().getName());
        }
    }

    private void handleDeviceAdded(String hubId, DeviceAddedEventAvro event) {
        String sensorId = event.getId();

        Sensor sensor = new Sensor();
        sensor.setId(sensorId);
        sensor.setHubId(hubId);

        sensorRepo.save(sensor);
        log.info("Saved sensor {} for hub {}", sensorId, hubId);
    }

    private void handleDeviceRemoved(DeviceRemovedEventAvro event) {
        String sensorId = event.getId();
        sensorRepo.deleteById(sensorId);
        log.info("Removed sensor {}", sensorId);
    }

    private void handleScenarioAdded(String hubId, ScenarioAddedEventAvro event) {
        Map<String, Condition> scenarioConditions = new HashMap<>();
        List<ScenarioConditionAvro> conditions = event.getConditions();

        for (ScenarioConditionAvro conditionAvro : conditions) {
            String sensorId = conditionAvro.getSensorId();

            Condition condition = new Condition();
            condition.setType(conditionAvro.getType());
            condition.setOperation(conditionAvro.getOperation());

            Object rawValue = conditionAvro.getValue();
            Integer value = null;
            if (rawValue instanceof Integer i) {
                value = i;
            } else if (rawValue instanceof Boolean b) {
                value = b ? 1 : 0;
            }
            condition.setValue(value);

            conditionRepo.save(condition);
            scenarioConditions.put(sensorId, condition);
        }

        Map<String, Action> scenarioActions = new HashMap<>();
        List<DeviceActionAvro> actions = event.getActions();

        for (DeviceActionAvro actionAvro : actions) {
            String sensorId = actionAvro.getSensorId();

            Action action = new Action();
            action.setType(actionAvro.getType());

            if (actionAvro.getType() == ActionTypeAvro.SET_VALUE && actionAvro.getValue() != null) {
                action.setValue(actionAvro.getValue());
            } else {
                action.setValue(null);
            }

            actionRepo.save(action);
            scenarioActions.put(sensorId, action);
        }

        Scenario scenario = scenarioRepo
                .findByHubIdAndName(hubId, event.getName())
                .orElseGet(Scenario::new);

        scenario.setHubId(hubId);
        scenario.setName(event.getName());
        scenario.setConditions(scenarioConditions);
        scenario.setActions(scenarioActions);

        scenarioRepo.save(scenario);

        log.info("Saved scenario '{}' for hub {} ({} conditions, {} actions)",
                scenario.getName(), hubId, scenarioConditions.size(), scenarioActions.size());
    }

    private void handleScenarioRemoved(String hubId, ScenarioRemovedEventAvro event) {
        scenarioRepo.findByHubIdAndName(hubId, event.getName())
                .ifPresent(scenario -> {
                    scenarioRepo.delete(scenario);
                    log.info("Removed scenario '{}' for hub {}", event.getName(), hubId);
                });
    }
}
