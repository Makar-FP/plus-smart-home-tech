package ru.yandex.practicum.telemetry.analyzer.service.logic;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.kafka.telemetry.event.*;
import ru.yandex.practicum.telemetry.analyzer.persistence.model.Action;
import ru.yandex.practicum.telemetry.analyzer.persistence.model.Condition;
import ru.yandex.practicum.telemetry.analyzer.persistence.model.Scenario;
import ru.yandex.practicum.telemetry.analyzer.persistence.model.Sensor;
import ru.yandex.practicum.telemetry.analyzer.persistence.repo.ScenarioRepo;
import ru.yandex.practicum.telemetry.analyzer.persistence.repo.SensorRepo;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class HubHandlerService {

    private final SensorRepo sensorRepo;
    private final ScenarioRepo scenarioRepo;

    public void handleRecord(HubEventAvro event) {
        Object payload = event.getPayload();
        String hubId = event.getHubId();

        try {
            if (payload instanceof DeviceAddedEventAvro deviceAdded) {
                handleDeviceAdded(hubId, deviceAdded);
            } else if (payload instanceof DeviceRemovedEventAvro deviceRemoved) {
                handleDeviceRemoved(hubId, deviceRemoved);
            } else if (payload instanceof ScenarioAddedEventAvro scenarioAdded) {
                handleScenarioAdded(hubId, scenarioAdded);
            } else if (payload instanceof ScenarioRemovedEventAvro scenarioRemoved) {
                handleScenarioRemoved(hubId, scenarioRemoved);
            } else {
                log.warn("Unsupported hub event payload type: {}", payload.getClass().getSimpleName());
            }
        } catch (Exception e) {
            log.error("Error while handling hub event for hub {}", hubId, e);
        }
    }

    private void handleDeviceAdded(String hubId, DeviceAddedEventAvro event) {
        String sensorId = event.getId();

        Optional<Sensor> existing = sensorRepo.findById(sensorId);
        if (existing.isPresent()) {
            log.debug("Sensor {} for hub {} already exists, skipping", sensorId, hubId);
            return;
        }

        Sensor sensor = new Sensor();
        sensor.setId(sensorId);
        sensor.setHubId(hubId);

        sensorRepo.save(sensor);
        log.info("Sensor {} registered for hub {}", sensorId, hubId);
    }

    private void handleDeviceRemoved(String hubId, DeviceRemovedEventAvro event) {
        String sensorId = event.getId();
        if (sensorRepo.existsById(sensorId)) {
            sensorRepo.deleteById(sensorId);
            log.info("Sensor {} removed for hub {}", sensorId, hubId);
        } else {
            log.debug("Sensor {} for hub {} does not exist, nothing to remove", sensorId, hubId);
        }
    }

    private void handleScenarioAdded(String hubId, ScenarioAddedEventAvro event) {
        String name = event.getName();

        Scenario scenario = scenarioRepo
                .findByHubIdAndName(hubId, name)
                .orElseGet(Scenario::new);

        scenario.setHubId(hubId);
        scenario.setName(name);

        Map<String, Condition> conditions = new HashMap<>();
        for (ScenarioConditionAvro condAvro : event.getConditions()) {
            Condition condition = new Condition();
            condition.setType(condAvro.getType());
            condition.setOperation(condAvro.getOperation());

            Object rawValue = condAvro.getValue();
            int storedValue;
            if (rawValue == null) {
                storedValue = 0;
            } else if (rawValue instanceof Integer i) {
                storedValue = i;
            } else if (rawValue instanceof Boolean b) {
                storedValue = b ? 1 : 0;
            } else {
                throw new IllegalArgumentException(
                        "Unsupported condition value type: " + rawValue.getClass().getName()
                );
            }

            condition.setValue(storedValue);
            conditions.put(condAvro.getSensorId(), condition);
        }

        Map<String, Action> actions = new HashMap<>();
        for (DeviceActionAvro actionAvro : event.getActions()) {
            Action action = new Action();
            action.setType(actionAvro.getType());
            Integer value = actionAvro.getValue();
            action.setValue(value != null ? value : 0);
            actions.put(actionAvro.getSensorId(), action);
        }

        scenario.setConditions(conditions);
        scenario.setActions(actions);

        scenarioRepo.save(scenario);
        log.info(
                "Stored/updated scenario '{}' for hub {} (conditions={}, actions={})",
                name, hubId, conditions.size(), actions.size()
        );
    }

    private void handleScenarioRemoved(String hubId, ScenarioRemovedEventAvro event) {
        String name = event.getName();
        scenarioRepo.findByHubIdAndName(hubId, name).ifPresentOrElse(
                s -> {
                    scenarioRepo.delete(s);
                    log.info("Scenario '{}' removed for hub {}", name, hubId);
                },
                () -> log.debug("Scenario '{}' for hub {} not found, nothing to remove", name, hubId)
        );
    }
}
