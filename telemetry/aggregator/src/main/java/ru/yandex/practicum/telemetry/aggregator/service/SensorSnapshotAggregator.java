package ru.yandex.practicum.telemetry.aggregator.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.avro.specific.SpecificRecordBase;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
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

            int count = 0;

            while (true) {
                ConsumerRecords<String, SpecificRecordBase> records =
                        consumer.poll(Duration.ofMillis(100));

                for (ConsumerRecord<String, SpecificRecordBase> record : records) {
                    log.info("Updating snapshot...");
                    Optional<SensorsSnapshotAvro> snapshotAvro =
                            service.updateState((SensorEventAvro) record.value());

                    if (snapshotAvro.isEmpty()) {
                        log.info("Snapshot was not updated (no changes detected).");
                    } else {
                        SensorsSnapshotAvro snapshot = snapshotAvro.get();
                        log.info("New snapshot: {}", snapshot);

                        ProducerRecord<String, SpecificRecordBase> snapshotRecord =
                                new ProducerRecord<>(
                                        EventTopic.TELEMETRY_SNAPSHOT_TOPIC,
                                        snapshot.getHubId(),
                                        snapshot
                                );

                        client.getProducer().send(snapshotRecord);
                        log.info("Snapshot sent to topic {}", EventTopic.TELEMETRY_SNAPSHOT_TOPIC);
                    }

                    log.debug("... start manageOffsets");
                    manageOffsets(record, count, consumer);
                    count++;
                    log.debug("... stop manageOffsets");
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

    private void manageOffsets(ConsumerRecord<String, SpecificRecordBase> record,
                               int count,
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
