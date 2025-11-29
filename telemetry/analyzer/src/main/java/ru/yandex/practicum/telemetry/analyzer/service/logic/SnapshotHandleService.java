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
import ru.yandex.practicum.telemetry.analyzer.persistence.repo.SensorRepo;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class SnapshotHandleService {

    private final SensorRepo sensorRepo;
    private final ScenarioRepo scenarioRepo;

    @GrpcClient("hub-router")
    private HubRouterControllerGrpc.HubRouterControllerBlockingStub hubRouterClient;

    public void handleRecord(SensorsSnapshotAvro snapshot) {
        try {
            String hubId = snapshot.getHubId();
            Map<String, SensorStateAvro> sensorsState = snapshot.getSensorsState();

            if (sensorsState == null || sensorsState.isEmpty()) {
                log.debug("Empty sensors state for hub {}, skip snapshot", hubId);
                return;
            }

            List<Scenario> scenarios = scenarioRepo.findByHubId(hubId);
            if (scenarios.isEmpty()) {
                log.debug("No scenarios found for hub {}, skip snapshot", hubId);
                return;
            }

            for (Scenario scenario : scenarios) {
                Map<String, Condition> conditions = scenario.getConditions();
                if (conditions == null || conditions.isEmpty()) {
                    log.debug("Scenario '{}' for hub {} has no conditions, skip", scenario.getName(), hubId);
                    continue;
                }

                if (!sensorRepo.existsByIdInAndHubId(conditions.keySet(), hubId)) {
                    log.debug("Scenario '{}' has sensors not belonging to hub {}, skipping",
                            scenario.getName(), hubId);
                    continue;
                }

                if (!sensorsState.keySet().containsAll(conditions.keySet())) {
                    log.debug("Snapshot for hub {} does not contain all sensors required for scenario '{}'",
                            hubId, scenario.getName());
                    continue;
                }

                boolean allConditionsTrue = conditions.entrySet().stream()
                        .allMatch(entry -> isConditionSatisfied(entry.getValue(), sensorsState.get(entry.getKey())));

                if (allConditionsTrue) {
                    log.info("All conditions satisfied for scenario '{}' on hub {}, executing actions",
                            scenario.getName(), hubId);
                    executeActions(hubId, scenario.getName(), scenario.getActions());
                } else {
                    log.debug("Conditions NOT satisfied for scenario '{}' on hub {}", scenario.getName(), hubId);
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

        return switch (condition.getType()) {
            case SWITCH -> {
                SwitchSensorAvro data = (SwitchSensorAvro) state.getData();
                boolean expectedOn = condition.getValue() == 1;
                yield data.getState() == expectedOn;
            }
            case MOTION -> {
                MotionSensorAvro data = (MotionSensorAvro) state.getData();
                boolean expectedMotion = condition.getValue() == 1;
                yield data.getMotion() == expectedMotion;
            }
            case LUMINOSITY -> {
                LightSensorAvro data = (LightSensorAvro) state.getData();
                yield compareNumeric(condition.getOperation(), condition.getValue(), data.getLuminosity());
            }
            case CO2_LEVEL -> {
                ClimateSensorAvro data = (ClimateSensorAvro) state.getData();
                yield compareNumeric(condition.getOperation(), condition.getValue(), data.getCo2Level());
            }
            case HUMIDITY -> {
                ClimateSensorAvro data = (ClimateSensorAvro) state.getData();
                yield compareNumeric(condition.getOperation(), condition.getValue(), data.getHumidity());
            }
            case TEMPERATURE -> {
                Object raw = state.getData();
                int actualTemperature;

                if (raw instanceof TemperatureSensorAvro t) {
                    actualTemperature = t.getTemperatureC();
                } else if (raw instanceof ClimateSensorAvro c) {
                    actualTemperature = c.getTemperatureC();
                } else {
                    yield false;
                }

                yield compareNumeric(condition.getOperation(), condition.getValue(), actualTemperature);
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
            log.debug("No actions for scenario '{}' on hub {}, nothing to execute", scenarioName, hubId);
            return;
        }

        Instant now = Instant.now();
        Timestamp timestamp = Timestamp.newBuilder()
                .setSeconds(now.getEpochSecond())
                .setNanos(now.getNano())
                .build();

        actions.forEach((sensorId, action) -> {
            try {
                ActionTypeProto protoType = ActionTypeProto.valueOf(action.getType().name());

                DeviceActionProto.Builder actionBuilder = DeviceActionProto.newBuilder()
                        .setSensorId(sensorId)
                        .setType(protoType);

                if (action.getType() == ActionTypeAvro.SET_VALUE) {
                    actionBuilder.setValue(action.getValue());
                }

                DeviceActionRequest request = DeviceActionRequest.newBuilder()
                        .setHubId(hubId)
                        .setScenarioName(scenarioName)
                        .setAction(actionBuilder.build())
                        .setTimestamp(timestamp)
                        .build();

                log.info("Sending device action via gRPC: hubId={}, scenario='{}', sensorId={}, type={}, value={}",
                        hubId, scenarioName, sensorId, action.getType(), action.getValue());

                hubRouterClient.handleDeviceAction(request);
            } catch (IllegalArgumentException e) {
                log.error("Failed to map action type {} to proto enum for sensor {} in scenario '{}'",
                        action.getType(), sensorId, scenarioName, e);
            } catch (Exception e) {
                log.error("Failed to send device action for sensor {} in scenario '{}' on hub {}",
                        sensorId, scenarioName, hubId, e);
            }
        });
    }
}
