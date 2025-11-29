package ru.yandex.practicum.telemetry.analyzer.service.logic;

import com.google.protobuf.Timestamp;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.grpc.telemetry.event.ActionTypeProto;
import ru.yandex.practicum.grpc.telemetry.event.DeviceActionProto;
import ru.yandex.practicum.grpc.telemetry.event.DeviceActionRequest;
import ru.yandex.practicum.grpc.telemetry.hubrouter.HubRouterControllerGrpc;
import ru.yandex.practicum.kafka.telemetry.event.*;
import ru.yandex.practicum.telemetry.analyzer.persistence.model.Action;
import ru.yandex.practicum.telemetry.analyzer.persistence.model.Condition;
import ru.yandex.practicum.telemetry.analyzer.persistence.model.Scenario;
import ru.yandex.practicum.telemetry.analyzer.persistence.repo.ScenarioRepo;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class SnapshotHandleService {

    private final ScenarioRepo scenarioRepo;

    @GrpcClient("hub-router")
    private HubRouterControllerGrpc.HubRouterControllerBlockingStub hubRouterClient;

    public void handleRecord(SensorsSnapshotAvro snapshot) {
        try {
            String hubId = snapshot.getHubId();
            Map<String, SensorStateAvro> sensorsState = snapshot.getSensorsState();

            if (sensorsState == null || sensorsState.isEmpty()) {
                log.debug("Snapshot for hub {} has empty sensors state, nothing to do", hubId);
                return;
            }

            List<Scenario> scenarios = scenarioRepo.findByHubId(hubId);
            if (scenarios == null || scenarios.isEmpty()) {
                log.debug("No scenarios found for hub {}, nothing to do", hubId);
                return;
            }

            for (Scenario scenario : scenarios) {
                Map<String, Condition> conditions = scenario.getConditions();
                if (conditions == null || conditions.isEmpty()) {
                    continue;
                }

                List<Boolean> results = new ArrayList<>();

                for (Map.Entry<String, Condition> entry : conditions.entrySet()) {
                    String sensorId = entry.getKey();
                    Condition condition = entry.getValue();
                    SensorStateAvro state = sensorsState.get(sensorId);

                    if (state == null) {
                        log.debug("Scenario '{}', hub {}: no state for sensor {}, skipping condition",
                                scenario.getName(), hubId, sensorId);
                        continue;
                    }

                    boolean result = isConditionSatisfied(condition, state);

                    log.debug("Scenario '{}', hub {}, sensor {}: type={}, op={}, value={} -> {}",
                            scenario.getName(), hubId, sensorId,
                            condition.getType(), condition.getOperation(), condition.getValue(),
                            result);

                    results.add(result);
                }

                if (!results.isEmpty() && results.stream().allMatch(Boolean::booleanValue)) {
                    log.info("All conditions satisfied for scenario '{}' on hub {}, executing actions",
                            scenario.getName(), hubId);
                    executeActions(hubId, scenario.getName(), scenario.getActions());
                }
            }
        } catch (Exception e) {
            log.error("Error while handling snapshot", e);
        }
    }

    private boolean isConditionSatisfied(Condition condition, SensorStateAvro state) {
        if (state == null || state.getData() == null) {
            return false;
        }

        Object data = state.getData();
        ConditionTypeAvro type = condition.getType();
        ConditionOperationAvro operation = condition.getOperation();
        int expected = condition.getValue() != null ? condition.getValue() : 0;

        return switch (type) {
            case SWITCH -> {
                if (!(data instanceof SwitchSensorAvro sensor)) {
                    yield false;
                }
                boolean expectedOn = expected == 1;
                yield sensor.getState() == expectedOn;
            }
            case MOTION -> {
                if (!(data instanceof MotionSensorAvro sensor)) {
                    yield false;
                }
                boolean expectedMotion = expected == 1;
                yield sensor.getMotion() == expectedMotion;
            }
            case LUMINOSITY -> {
                if (!(data instanceof LightSensorAvro sensor)) {
                    yield false;
                }
                yield compareNumeric(operation, expected, sensor.getLuminosity());
            }
            case TEMPERATURE -> {
                int actualTemperature;
                if (data instanceof TemperatureSensorAvro t) {
                    actualTemperature = t.getTemperatureC();
                } else if (data instanceof ClimateSensorAvro c) {
                    actualTemperature = c.getTemperatureC();
                } else {
                    yield false;
                }
                yield compareNumeric(operation, expected, actualTemperature);
            }
            case CO2_LEVEL -> {
                if (!(data instanceof ClimateSensorAvro c)) {
                    yield false;
                }
                yield compareNumeric(operation, expected, c.getCo2Level());
            }
            case HUMIDITY -> {
                if (!(data instanceof ClimateSensorAvro c)) {
                    yield false;
                }
                yield compareNumeric(operation, expected, c.getHumidity());
            }
        };
    }

    private boolean compareNumeric(ConditionOperationAvro operation, int expected, int actual) {
        return switch (operation) {
            case EQUALS -> actual == expected;
            case GREATER_THAN -> actual > expected;
            case LOWER_THAN -> actual < expected;
        };
    }

    private void executeActions(String hubId, String scenarioName, Map<String, Action> actions) {
        if (actions == null || actions.isEmpty()) {
            log.debug("Scenario '{}' on hub {} has no actions", scenarioName, hubId);
            return;
        }

        if (hubRouterClient == null) {
            log.error("hubRouterClient is null, cannot send commands to Hub Router");
            return;
        }

        Instant now = Instant.now();
        Timestamp timestamp = Timestamp.newBuilder()
                .setSeconds(now.getEpochSecond())
                .setNanos(now.getNano())
                .build();

        actions.forEach((sensorId, action) -> {
            try {
                DeviceActionProto.Builder actionBuilder = DeviceActionProto.newBuilder()
                        .setSensorId(sensorId)
                        .setType(ActionTypeProto.valueOf(action.getType().name()));

                if (action.getType() == ActionTypeAvro.SET_VALUE && action.getValue() != null) {
                    actionBuilder.setValue(action.getValue());
                }

                DeviceActionRequest request = DeviceActionRequest.newBuilder()
                        .setHubId(hubId)
                        .setScenarioName(scenarioName)
                        .setAction(actionBuilder.build())
                        .setTimestamp(timestamp)
                        .build();

                hubRouterClient.handleDeviceAction(request);

                log.info("Sent DeviceAction to Hub Router: hubId={}, scenario='{}', sensorId={}, type={}, value={}",
                        hubId, scenarioName, sensorId, action.getType(), action.getValue());
            } catch (Exception e) {
                log.error("Failed to send action for sensor {} in scenario '{}' on hub {}",
                        sensorId, scenarioName, hubId, e);
            }
        });
    }
}
