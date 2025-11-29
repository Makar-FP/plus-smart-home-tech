package ru.yandex.practicum.telemetry.analyzer.service.logic;

import com.google.protobuf.Timestamp;
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
public class SnapshotHandleService {

    private final SensorRepo sensorRepo;
    private final ScenarioRepo scenarioRepo;
    private final HubRouterControllerGrpc.HubRouterControllerBlockingStub hubRouterClient;

    public SnapshotHandleService(
            @GrpcClient("hub-router") HubRouterControllerGrpc.HubRouterControllerBlockingStub hubRouterClient,
            SensorRepo sensorRepo,
            ScenarioRepo scenarioRepo, HubRouterControllerGrpc.HubRouterControllerBlockingStub hubRouterClient1) {
        this.sensorRepo = sensorRepo;
        this.scenarioRepo = scenarioRepo;
        this.hubRouterClient = hubRouterClient1;
    }

    public void handleRecord(SensorsSnapshotAvro snapshot) {
        try {
            String hubId = snapshot.getHubId();
            Map<String, SensorStateAvro> sensorsState = snapshot.getSensorsState();

            if (sensorsState == null || sensorsState.isEmpty()) {
                return;
            }

            List<Scenario> scenarios = scenarioRepo.findByHubId(hubId);
            if (scenarios.isEmpty()) {
                return;
            }

            for (Scenario scenario : scenarios) {
                Map<String, Condition> conditions = scenario.getConditions();
                if (conditions == null || conditions.isEmpty()) {
                    continue;
                }

                if (!sensorRepo.existsByIdInAndHubId(conditions.keySet(), hubId)) {
                    log.debug("Scenario '{}' has sensors that do not belong to hub {}, skipping",
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
                var raw = state.getData();
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
            return;
        }

        Instant now = Instant.now();
        Timestamp timestamp = Timestamp.newBuilder()
                .setSeconds(now.getEpochSecond())
                .setNanos(now.getNano())
                .build();

        actions.forEach((sensorId, action) -> {
            DeviceActionProto.Builder actionBuilder = DeviceActionProto.newBuilder()
                    .setSensorId(sensorId)
                    .setType(ActionTypeProto.valueOf(action.getType().name()));

            if (action.getType() == ActionTypeAvro.SET_VALUE) {
                actionBuilder.setValue(action.getValue());
            }

            DeviceActionRequest request = DeviceActionRequest.newBuilder()
                    .setHubId(hubId)
                    .setScenarioName(scenarioName)
                    .setAction(actionBuilder.build())
                    .setTimestamp(timestamp)
                    .build();

            hubRouterClient.handleDeviceAction(request);
        });
    }
}

