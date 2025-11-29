package ru.yandex.practicum.telemetry.collector.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.avro.specific.SpecificRecordBase;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.telemetry.collector.config.EventClient;
import ru.yandex.practicum.telemetry.collector.config.EventTopic;

@Slf4j
@Service
@AllArgsConstructor
public class KafkaEventProducer {
    private final EventClient client;

    public void sendHubEventToKafka(SpecificRecordBase message) {
        sendEventToKafka(EventTopic.TELEMETRY_HUB_TOPIC, "HubEventAvro", message);
    }

    public void sendSensorEventToKafka(SpecificRecordBase message) {
        sendEventToKafka(EventTopic.TELEMETRY_SENSOR_TOPIC, "SensorEventAvro", message);
    }

    private void sendEventToKafka(String topic, String eventType, SpecificRecordBase message) {
        ProducerRecord<String, SpecificRecordBase> record = new ProducerRecord<>(topic, message);
        log.info("--> Sending message to Kafka ({}): topic={}, value={}", eventType, topic, message);
        client.getProducer().send(record, (metadata, exception) -> {
            if (exception != null) {
                log.error("Failed to send message to Kafka ({}), topic={}", eventType, topic, exception);
            } else {
                log.debug("Message sent to Kafka successfully ({}): topic={}, partition={}, offset={}",
                        eventType, metadata.topic(), metadata.partition(), metadata.offset());
            }
        });
    }
}
