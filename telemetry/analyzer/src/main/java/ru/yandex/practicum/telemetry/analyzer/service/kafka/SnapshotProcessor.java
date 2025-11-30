package ru.yandex.practicum.telemetry.analyzer.service.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.common.errors.WakeupException;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.VoidDeserializer;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.kafka.telemetry.event.SensorsSnapshotAvro;
import ru.yandex.practicum.telemetry.analyzer.config.SnapshotEventDeserializer;
import ru.yandex.practicum.telemetry.analyzer.service.logic.SnapshotHandleService;

import java.time.Duration;
import java.util.List;
import java.util.Properties;

@Slf4j
@Component
@RequiredArgsConstructor
public class SnapshotProcessor implements Runnable {

    private static final Duration POLL_TIMEOUT = Duration.ofMillis(100);

    private volatile boolean running = true;
    private KafkaConsumer<String, SensorsSnapshotAvro> consumer;

    private final SnapshotHandleService snapshotHandleService;

    public void shutdown() {
        running = false;
        if (consumer != null) {
            consumer.wakeup();
        }
    }

    @Override
    public void run() {
        consumer = new KafkaConsumer<>(getConsumerProperties());
        consumer.subscribe(List.of("telemetry.snapshots.v1"));

        try {
            while (running) {
                ConsumerRecords<String, SensorsSnapshotAvro> records = consumer.poll(POLL_TIMEOUT);

                for (ConsumerRecord<String, SensorsSnapshotAvro> record : records) {
                    snapshotHandleService.handleRecord(record.value());
                }
                consumer.commitSync();
            }
        } catch (WakeupException e) {
            if (running) {
                log.error("Snapshot consumer was unexpectedly woken up", e);
            }
        } catch (Exception e) {
            log.error("Error while processing snapshots", e);
        } finally {
            try {
                consumer.commitSync();
            } catch (Exception e) {
                log.warn("Error while committing offsets for snapshots during shutdown", e);
            }
            log.info("Closing snapshot consumer");
            consumer.close();
        }
    }

    private static Properties getConsumerProperties() {
        Properties properties = new Properties();
        properties.put(ConsumerConfig.CLIENT_ID_CONFIG, "SnapshotConsumer");
        properties.put(ConsumerConfig.GROUP_ID_CONFIG, "consumer-client-snapshot");
        properties.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        properties.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false");
        properties.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getCanonicalName());
        properties.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, SnapshotEventDeserializer.class.getCanonicalName());
        return properties;
    }
}
