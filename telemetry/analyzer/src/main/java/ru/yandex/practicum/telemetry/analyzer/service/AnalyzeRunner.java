package ru.yandex.practicum.telemetry.analyzer.service;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.telemetry.analyzer.service.kafka.HubEventProcessor;
import ru.yandex.practicum.telemetry.analyzer.service.kafka.SnapshotProcessor;

@Component
@RequiredArgsConstructor
public class AnalyzeRunner implements CommandLineRunner {

    private final HubEventProcessor hubEventProcessor;
    private final SnapshotProcessor snapshotProcessor;

    @Override
    public void run(String... args) {
        Thread hubEventThread = new Thread(hubEventProcessor, "HubEventHandlerThread");
        hubEventThread.start();

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            hubEventProcessor.shutdown();
            snapshotProcessor.shutdown();
            try {
                hubEventThread.join();
            } catch (InterruptedException ignored) {
                Thread.currentThread().interrupt();
            }
        }));

        snapshotProcessor.run();
    }
}
