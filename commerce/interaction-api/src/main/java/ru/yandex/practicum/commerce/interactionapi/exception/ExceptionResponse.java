package ru.yandex.practicum.commerce.interactionapi.exception;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class ExceptionResponse {

    private String status;

    private String reason;

    private String message;

    private LocalDateTime timestamp;
}
