package ru.yandex.practicum.telemetry.collector.controller;

import com.google.protobuf.Empty;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;
import ru.yandex.practicum.grpc.telemetry.collector.CollectorControllerGrpc;
import ru.yandex.practicum.grpc.telemetry.event.HubEventProto;
import ru.yandex.practicum.grpc.telemetry.event.SensorEventProto;
import ru.yandex.practicum.telemetry.collector.service.handler.HubEventHandler;
import ru.yandex.practicum.telemetry.collector.service.handler.SensorEventHandler;

import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@GrpcService
public class EventController extends CollectorControllerGrpc.CollectorControllerImplBase {

    private final Map<SensorEventProto.PayloadCase, SensorEventHandler> sensorEventHandlers;
    private final Map<HubEventProto.PayloadCase, HubEventHandler> hubEventHandlers;

    public EventController(Set<SensorEventHandler> sensorEventHandlers,
                           Set<HubEventHandler> hubEventHandlers) {

        this.sensorEventHandlers = sensorEventHandlers.stream()
                .collect(Collectors.toUnmodifiableMap(
                        SensorEventHandler::getMessageType,
                        Function.identity(),
                        (h1, h2) -> {
                            throw new IllegalStateException(
                                    "Multiple SensorEventHandler beans found for type: " + h1.getMessageType()
                            );
                        }
                ));

        this.hubEventHandlers = hubEventHandlers.stream()
                .collect(Collectors.toUnmodifiableMap(
                        HubEventHandler::getMessageType,
                        Function.identity(),
                        (h1, h2) -> {
                            throw new IllegalStateException(
                                    "Multiple HubEventHandler beans found for type: " + h1.getMessageType()
                            );
                        }
                ));
    }

    @Override
    public void collectSensorEvent(SensorEventProto request, StreamObserver<Empty> responseObserver) {
        SensorEventProto.PayloadCase payloadCase = request.getPayloadCase();
        log.info("gRPC collectSensorEvent, payloadCase={}", payloadCase);

        if (payloadCase == SensorEventProto.PayloadCase.PAYLOAD_NOT_SET) {
            log.warn("Sensor event payload is not set: request={}", request);
            responseObserver.onError(
                    Status.INVALID_ARGUMENT
                            .withDescription("Sensor event payload must not be empty")
                            .asRuntimeException()
            );
            return;
        }

        SensorEventHandler handler = sensorEventHandlers.get(payloadCase);
        if (handler == null) {
            log.warn("No handler registered for sensor event payload type: {}", payloadCase);
            responseObserver.onError(
                    Status.INVALID_ARGUMENT
                            .withDescription("No handler registered for sensor event payload type: " + payloadCase)
                            .asRuntimeException()
            );
            return;
        }

        try {
            handler.handle(request);
            responseObserver.onNext(Empty.getDefaultInstance());
            responseObserver.onCompleted();
        } catch (Exception e) {
            log.error("Failed to handle sensor event. payloadCase={}, request={}", payloadCase, request, e);
            responseObserver.onError(
                    Status.INTERNAL
                            .withDescription("Internal error while processing sensor event")
                            .withCause(e)
                            .asRuntimeException()
            );
        }
    }

    @Override
    public void collectHubEvent(HubEventProto request, StreamObserver<Empty> responseObserver) {
        HubEventProto.PayloadCase payloadCase = request.getPayloadCase();
        log.info("gRPC collectHubEvent, payloadCase={}", payloadCase);

        if (payloadCase == HubEventProto.PayloadCase.PAYLOAD_NOT_SET) {
            log.warn("Hub event payload is not set: request={}", request);
            responseObserver.onError(
                    Status.INVALID_ARGUMENT
                            .withDescription("Hub event payload must not be empty")
                            .asRuntimeException()
            );
            return;
        }

        HubEventHandler handler = hubEventHandlers.get(payloadCase);
        if (handler == null) {
            log.warn("No handler registered for hub event payload type: {}", payloadCase);
            responseObserver.onError(
                    Status.INVALID_ARGUMENT
                            .withDescription("No handler registered for hub event payload type: " + payloadCase)
                            .asRuntimeException()
            );
            return;
        }

        try {
            handler.handle(request);
            responseObserver.onNext(Empty.getDefaultInstance());
            responseObserver.onCompleted();
        } catch (Exception e) {
            log.error("Failed to handle hub event. payloadCase={}, request={}", payloadCase, request, e);
            responseObserver.onError(
                    Status.INTERNAL
                            .withDescription("Internal error while processing hub event")
                            .withCause(e)
                            .asRuntimeException()
            );
        }
    }
}