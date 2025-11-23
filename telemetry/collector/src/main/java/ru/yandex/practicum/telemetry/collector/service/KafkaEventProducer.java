package ru.yandex.practicum.telemetry.collector.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.avro.specific.SpecificRecordBase;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.telemetry.collector.configuration.EventClient;

@Slf4j
@Service
@AllArgsConstructor
public class KafkaEventProducer {

    private final EventClient client;

    public void sendHubEventToKafka(SpecificRecordBase message) {
        String topic = "telemetry.hubs.v1";
        ProducerRecord<String, SpecificRecordBase> record =
                new ProducerRecord<>(topic, message);
        log.info("--> Sending message to Kafka (HubEventAvro): {}", record);
        client.getProducer().send(record);
    }

    public void sendSensorEventToKafka(SpecificRecordBase message) {
        String topic = "telemetry.sensors.v1";
        ProducerRecord<String, SpecificRecordBase> record =
                new ProducerRecord<>(topic, message);
        log.info("--> Sending message to Kafka (SensorEventAvro): {}", record);
        client.getProducer().send(record);
    }
}
