package ru.yandex.practicum.telemetry.aggregator.config;

import org.apache.avro.specific.SpecificRecordBase;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.producer.Producer;

public interface EventClient {

    Producer<String, SpecificRecordBase> getProducer();

    Consumer<String, SpecificRecordBase> getSensorConsumer();

    void stop();
}

