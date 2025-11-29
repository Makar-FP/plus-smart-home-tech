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
        var payload = record.getPayload();
        String hubId = record.getHubId();

        if (payload instanceof DeviceAddedEventAvro event) {
            handleDeviceAdded(hubId, event);
        } else if (payload instanceof DeviceRemovedEventAvro event) {
            handleDeviceRemoved(hubId, event);
        } else if (payload instanceof ScenarioAddedEventAvro event) {
            handleScenarioAdded(hubId, event);
        } else if (payload instanceof ScenarioRemovedEventAvro event) {
            handleScenarioRemoved(hubId, event);
        } else {
            log.warn("Unknown hub event payload type: {}",
                    payload != null ? payload.getClass().getName() : "null");
        }
    }

    private void handleDeviceAdded(String hubId, DeviceAddedEventAvro event) {
        String sensorId = event.getId();
        sensorRepo.findByIdAndHubId(sensorId, hubId)
                .ifPresentOrElse(
                        s -> log.debug("Sensor {} for hub {} already exists, skipping add", sensorId, hubId),
                        () -> {
                            Sensor sensor = new Sensor(sensorId, hubId);
                            sensorRepo.save(sensor);
                            log.info("Sensor {} added for hub {}", sensorId, hubId);
                        }
                );
    }

    private void handleDeviceRemoved(String hubId, DeviceRemovedEventAvro event) {
        String sensorId = event.getId();
        sensorRepo.findByIdAndHubId(sensorId, hubId)
                .ifPresent(sensor -> {
                    sensorRepo.delete(sensor);
                    log.info("Sensor {} removed for hub {}", sensorId, hubId);
                });
    }

    private void handleScenarioAdded(String hubId, ScenarioAddedEventAvro event) {
        Map<String, Condition> scenarioConditions = new HashMap<>();
        for (ScenarioConditionAvro conditionAvro : event.getConditions()) {
            Condition condition = new Condition();
            condition.setType(conditionAvro.getType());
            condition.setOperation(conditionAvro.getOperation());

            Object rawValue = conditionAvro.getValue();
            int value = (rawValue instanceof Boolean b) ? (b ? 1 : 0) : ((Number) rawValue).intValue();
            condition.setValue(value);

            conditionRepo.save(condition);
            scenarioConditions.put(conditionAvro.getSensorId(), condition);
        }

        Map<String, Action> scenarioActions = new HashMap<>();
        for (DeviceActionAvro actionAvro : event.getActions()) {
            Action action = new Action();
            action.setType(actionAvro.getType());
            action.setValue(actionAvro.getValue());
            actionRepo.save(action);

            scenarioActions.put(actionAvro.getSensorId(), action);
        }

        Scenario scenario = scenarioRepo
                .findByHubIdAndName(hubId, event.getName())
                .orElseGet(Scenario::new);

        scenario.setHubId(hubId);
        scenario.setName(event.getName());
        scenario.getConditions().clear();
        scenario.getConditions().putAll(scenarioConditions);
        scenario.getActions().clear();
        scenario.getActions().putAll(scenarioActions);

        scenarioRepo.save(scenario);
        log.info("Scenario '{}' for hub {} saved/updated", event.getName(), hubId);
    }

    private void handleScenarioRemoved(String hubId, ScenarioRemovedEventAvro event) {
        scenarioRepo.findByHubIdAndName(hubId, event.getName())
                .ifPresent(scenario -> {
                    scenarioRepo.delete(scenario);
                    log.info("Scenario '{}' for hub {} removed", event.getName(), hubId);
                });
    }
}

