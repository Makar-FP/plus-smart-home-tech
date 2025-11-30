package ru.yandex.practicum.telemetry.collector.config;

import org.apache.avro.io.BinaryEncoder;
import org.apache.avro.io.DatumWriter;
import org.apache.avro.io.EncoderFactory;
import org.apache.avro.specific.SpecificDatumWriter;
import org.apache.avro.specific.SpecificRecordBase;
import org.apache.kafka.common.errors.SerializationException;
import org.apache.kafka.common.serialization.Serializer;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class EventAvroSerializer<T extends SpecificRecordBase> implements Serializer<T> {

    private static final EncoderFactory ENCODER_FACTORY = EncoderFactory.get();

    @Override
    public byte[] serialize(String topic, T data) {
        if (data == null) {
            return null;
        }

        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            BinaryEncoder encoder = ENCODER_FACTORY.directBinaryEncoder(out, null);
            DatumWriter<T> writer = new SpecificDatumWriter<>(data.getSchema());

            writer.write(data, encoder);
            encoder.flush();

            return out.toByteArray();
        }  catch (IOException | RuntimeException ex) {
            String type = (data != null) ? data.getClass().getName() : "null";
            throw new SerializationException(
                    "Failed to serialize Avro message for topic [" + topic + "], type=[" + type + "]",
                    ex
            );
        }
    }
}
