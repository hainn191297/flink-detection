package com.example.detection.deserializer;

import java.io.IOException;

import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.connector.kafka.source.reader.deserializer.KafkaRecordDeserializationSchema;
import org.apache.flink.util.Collector;
import org.apache.kafka.clients.consumer.ConsumerRecord;

import com.example.detection.model.RuleTriggerEvent;
import com.fasterxml.jackson.databind.ObjectMapper;

public class RuleTriggerEventDeserializationSchema implements KafkaRecordDeserializationSchema<RuleTriggerEvent> {

    private static final ObjectMapper mapper = new ObjectMapper();

    @Override
    public void deserialize(ConsumerRecord<byte[], byte[]> consumerRecord, Collector<RuleTriggerEvent> out)
            throws IOException {
        if (consumerRecord.value() != null) {
            RuleTriggerEvent event = mapper.readValue(consumerRecord.value(), RuleTriggerEvent.class);
            out.collect(event);
        }
    }

    @Override
    public TypeInformation<RuleTriggerEvent> getProducedType() {
        return TypeInformation.of(RuleTriggerEvent.class);
    }
}
