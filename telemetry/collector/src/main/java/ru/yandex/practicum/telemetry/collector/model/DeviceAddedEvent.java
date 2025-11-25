package ru.yandex.practicum.telemetry.collector.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString(callSuper = true)
@NotNull
public class DeviceAddedEvent extends HubEvent {

    @NotBlank
    private String id;

    private DeviceType deviceType;


    @Override
    public HubEventType getType() {
        return HubEventType.DEVICE_ADDED;
    }
}
