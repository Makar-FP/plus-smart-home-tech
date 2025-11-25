package ru.yandex.practicum.telemetry.aggregator.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.avro.specific.SpecificRecordBase;
import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.errors.WakeupException;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.kafka.telemetry.event.SensorEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.SensorsSnapshotAvro;
import ru.yandex.practicum.telemetry.aggregator.config.EventClient;
import ru.yandex.practicum.telemetry.aggregator.config.EventTopic;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class SensorSnapshotAggregator {
    private final EventClient client;
    private final InMemorySensorEvent service;

    private final Map<TopicPartition, OffsetAndMetadata> currentOffsets = new HashMap<>();

    public void start() {
        Consumer<String, SpecificRecordBase> consumer = client.getSensorConsumer();

        try {
            consumer.subscribe(List.of(EventTopic.TELEMETRY_SENSOR_TOPIC));

            long processedCount = 0L;

            while (true) {
                ConsumerRecords<String, SpecificRecordBase> records =
                        consumer.poll(Duration.ofMillis(100));

                for (ConsumerRecord<String, SpecificRecordBase> record : records) {
                    processRecord(record);
                    manageOffsets(record, processedCount, consumer);
                    processedCount++;
                }

                consumer.commitAsync();
            }

        } catch (WakeupException ignored) {
        } catch (Exception e) {
            log.error("Error while processing sensor events", e);
        } finally {
            try {
                client.getProducer().flush();
                if (!currentOffsets.isEmpty()) {
                    consumer.commitSync(currentOffsets);
                }
            } finally {
                client.stop();
            }
        }
    }

    private void processRecord(ConsumerRecord<String, SpecificRecordBase> record) {
        log.info("Updating snapshot for sensor event, topic={}, partition={}, offset={}",
                record.topic(), record.partition(), record.offset());

        SensorEventAvro event = (SensorEventAvro) record.value();
        Optional<SensorsSnapshotAvro> snapshotAvro = service.updateState(event);

        if (snapshotAvro.isEmpty()) {
            log.debug("Snapshot was not updated (no changes detected).");
            return;
        }

        SensorsSnapshotAvro snapshot = snapshotAvro.get();
        log.info("New snapshot created/updated: {}", snapshot);

        ProducerRecord<String, SpecificRecordBase> snapshotRecord =
                new ProducerRecord<>(EventTopic.TELEMETRY_SNAPSHOT_TOPIC, snapshot);

        client.getProducer().send(snapshotRecord, (metadata, exception) -> {
            if (exception != null) {
                log.error("Failed to send snapshot to topic {}", EventTopic.TELEMETRY_SNAPSHOT_TOPIC, exception);
            } else {
                log.info("Snapshot sent to topic {}, partition={}, offset={}",
                        metadata.topic(), metadata.partition(), metadata.offset());
            }
        });
    }

    private void manageOffsets(ConsumerRecord<String, SpecificRecordBase> record,
                               long count,
                               Consumer<String, SpecificRecordBase> consumer) {
        currentOffsets.put(
                new TopicPartition(record.topic(), record.partition()),
                new OffsetAndMetadata(record.offset() + 1)
        );

        if (count % 10 == 0) {
            consumer.commitAsync(currentOffsets, (offsets, exception) -> {
                if (exception != null) {
                    log.warn("Error while committing offsets: {}", offsets, exception);
                }
            });
        }
    }
}
