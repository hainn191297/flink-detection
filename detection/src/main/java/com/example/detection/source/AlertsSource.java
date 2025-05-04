package com.example.detection.source;

import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.connector.kafka.source.KafkaSource;
import org.apache.flink.connector.kafka.source.enumerator.initializer.OffsetsInitializer;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.util.ParameterTool;

import com.example.detection.deserializer.AlertDeserializer;
import com.example.detection.helper.KafkaHelper;
import com.example.detection.model.Alert;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * AlertsSource:
 * Tạo Kafka source, decode Event → wrap thành Alert
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class AlertsSource {

    public static DataStream<Alert> create(StreamExecutionEnvironment env, ParameterTool params) {
        String brokers = params.get("kafka.source.bootstrap.servers", "localhost:9092");
        String topic = params.get("kafka.topic.alert", "alert-topic");
        String groupId = params.get("kafka.source.group.id", "detection-consumer");

        KafkaHelper.createTopicIfNotExists(brokers, topic, groupId, 1, (short) 1);

        KafkaSource<Alert> kafkaSource = KafkaSource.<Alert>builder()
                .setProperty("partition.discovery.interval.ms", "10000")
                .setBootstrapServers(brokers)
                .setTopics(topic)
                .setGroupId(groupId)
                .setStartingOffsets(OffsetsInitializer
                        .latest())
                .setValueOnlyDeserializer(new AlertDeserializer())
                .build();

        return env
                .fromSource(kafkaSource, WatermarkStrategy.noWatermarks(), "Alert-Single-Source")
                .name("Alert Single  → Alert Complex Mapper");
    }

}
