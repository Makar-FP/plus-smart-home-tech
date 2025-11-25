package ru.yandex.practicum.telemetry.collector.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import ru.yandex.practicum.telemetry.collector.model.HubEvent;
import ru.yandex.practicum.telemetry.collector.model.HubEventType;
import ru.yandex.practicum.telemetry.collector.model.SensorEvent;
import ru.yandex.practicum.telemetry.collector.model.SensorEventType;
import ru.yandex.practicum.telemetry.collector.service.handler.HubEventHandler;
import ru.yandex.practicum.telemetry.collector.service.handler.SensorEventHandler;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Validated
@RestController
@RequestMapping("/events")
public class EventController {

    private final Map<SensorEventType, SensorEventHandler> sensorEventHandlers;
    private final Map<HubEventType, HubEventHandler> hubEventHandlers;

    public EventController(Set<SensorEventHandler> sensorEventHandlers,
                           Set<HubEventHandler> hubEventHandlers) {
        this.sensorEventHandlers = sensorEventHandlers.stream()
                .collect(Collectors.toUnmodifiableMap(
                        SensorEventHandler::getMessageType,
                        handler -> handler,
                        (h1, h2) -> {
                            throw new IllegalStateException(
                                    "Найдено два SensorEventHandler для типа: " + h1.getMessageType()
                            );
                        }
                ));

        this.hubEventHandlers = hubEventHandlers.stream()
                .collect(Collectors.toUnmodifiableMap(
                        HubEventHandler::getMessageType,
                        handler -> handler,
                        (h1, h2) -> {
                            throw new IllegalStateException(
                                    "Найдено два HubEventHandler для типа: " + h1.getMessageType()
                            );
                        }
                ));
    }

    @PostMapping("/sensors")
    @ResponseStatus(HttpStatus.CREATED)
    public void collectSensorEvent(@Valid @RequestBody SensorEvent request) {
        log.info("POST /events/sensors, body={}", request);

        SensorEventType type = request.getType();
        SensorEventHandler handler = sensorEventHandlers.get(type);

        if (handler == null) {
            log.warn("Нет обработчика для типа сенсорного события: {}", type);
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Не найден обработчик для типа сенсорного события: " + type
            );
        }

        handler.handle(request);
    }

    @PostMapping("/hubs")
    @ResponseStatus(HttpStatus.CREATED)
    public void collectHubEvent(@Valid @RequestBody HubEvent request) {
        log.info("POST /events/hubs, body={}", request);

        HubEventType type = request.getType();
        HubEventHandler handler = hubEventHandlers.get(type);

        if (handler == null) {
            log.warn("Нет обработчика для типа события хаба: {}", type);
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Не найден обработчик для типа события хаба: " + type
            );
        }

        handler.handle(request);
    }
}