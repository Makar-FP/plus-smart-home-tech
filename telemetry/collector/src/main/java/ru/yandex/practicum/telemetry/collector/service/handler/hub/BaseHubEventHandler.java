package ru.yandex.practicum.telemetry.collector.service.handler.hub;

import lombok.extern.slf4j.Slf4j;
import org.apache.avro.specific.SpecificRecordBase;
import ru.yandex.practicum.grpc.telemetry.event.HubEventProto;
import ru.yandex.practicum.kafka.telemetry.event.HubEventAvro;
import ru.yandex.practicum.telemetry.collector.service.KafkaEventProducer;
import ru.yandex.practicum.telemetry.collector.service.handler.HubEventHandler;

@Slf4j
public abstract class BaseHubEventHandler implements HubEventHandler {

    protected final KafkaEventProducer producer;

    protected BaseHubEventHandler(KafkaEventProducer producer) {
        this.producer = producer;
    }

    /**
     * Maps incoming gRPC event to Avro record to be published to Kafka.
     */
    protected abstract HubEventAvro mapToAvro(HubEventProto event);

    @Override
    public void handle(HubEventProto event) {
        HubEventAvro record = mapToAvro(event);
        if (record == null) {
            log.warn("Handler {} produced null Avro record for event: {}",
                    getClass().getSimpleName(), event);
            return;
        }
        producer.sendHubEventToKafka(record);
    }
}
