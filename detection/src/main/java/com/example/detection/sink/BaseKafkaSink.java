package com.example.detection.sink;

import org.apache.flink.api.common.serialization.SimpleStringSchema;
import org.apache.flink.connector.kafka.sink.KafkaRecordSerializationSchema;
import org.apache.flink.connector.kafka.sink.KafkaSink;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.util.ParameterTool;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Base class for all Kafka sinks providing common functionality
 */
public abstract class BaseKafkaSink {
    protected static final Logger log = LogManager.getLogger(BaseKafkaSink.class);
    protected static final ObjectMapper mapper = new ObjectMapper();

    protected BaseKafkaSink() {
    }

    protected static KafkaSink<String> createKafkaSink(String brokers, String topic, String prefix) {
        return KafkaSink.<String>builder()
                .setBootstrapServers(brokers)
                .setRecordSerializer(KafkaRecordSerializationSchema.builder()
                        .setTopic(topic)
                        .setValueSerializationSchema(new SimpleStringSchema())
                        .build())
                .setTransactionalIdPrefix(prefix)
                .build();
    }

    protected static String getKafkaBrokers(ParameterTool params) {
        return params.get("kafka.sink.bootstrap.servers", "localhost:9092");
    }

    protected static <T> DataStream<String> toJsonStream(DataStream<T> stream, String operationName) {
        return stream.map(value -> {
            try {
                return mapper.writeValueAsString(value);
            } catch (Exception e) {
                log.error("Failed to serialize value: {}", value, e);
                return null;
            }
        })
                .name(operationName);
    }
}