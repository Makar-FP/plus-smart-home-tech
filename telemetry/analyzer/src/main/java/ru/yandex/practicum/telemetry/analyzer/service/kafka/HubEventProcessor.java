package ru.yandex.practicum.telemetry.analyzer.service.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.common.errors.WakeupException;
import org.apache.kafka.common.serialization.VoidDeserializer;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.kafka.telemetry.event.HubEventAvro;
import ru.yandex.practicum.telemetry.analyzer.config.HubEventDeserializer;
import ru.yandex.practicum.telemetry.analyzer.service.logic.HubHandlerService;

import java.time.Duration;
import java.util.List;
import java.util.Properties;

@Slf4j
@Component
@RequiredArgsConstructor
public class HubEventProcessor implements Runnable {

    private static final Duration POLL_TIMEOUT = Duration.ofMillis(100);

    private volatile boolean running = true;
    private KafkaConsumer<Void, HubEventAvro> consumer;

    private final HubHandlerService hubHandlerService;

    public void shutdown() {
        running = false;
        if (consumer != null) {
            consumer.wakeup();
        }
    }

    @Override
    public void run() {
        consumer = new KafkaConsumer<>(getConsumerProperties());
        consumer.subscribe(List.of("telemetry.hubs.v1"));

        try {
            while (running) {
                ConsumerRecords<Void, HubEventAvro> records = consumer.poll(POLL_TIMEOUT);

                for (ConsumerRecord<Void, HubEventAvro> record : records) {
                    hubHandlerService.handleRecord(record.value());
                }

                consumer.commitAsync();
            }
        } catch (WakeupException e) {
            if (running) {
                log.error("Hub events consumer was unexpectedly woken up", e);
            }
        } catch (Exception e) {
            log.error("Error while processing hub events", e);
        } finally {
            try {
                consumer.commitSync();
            } catch (Exception e) {
                log.warn("Error while committing offsets for hub events during shutdown", e);
            }
            log.info("Closing hub events consumer");
            consumer.close();
        }
    }

    private static Properties getConsumerProperties() {
        Properties properties = new Properties();
        properties.put(ConsumerConfig.CLIENT_ID_CONFIG, "HubEventConsumer");
        properties.put(ConsumerConfig.GROUP_ID_CONFIG, "consumer-client-hub");
        properties.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        properties.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, VoidDeserializer.class.getCanonicalName());
        properties.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, HubEventDeserializer.class.getCanonicalName());
        return properties;
    }
}
