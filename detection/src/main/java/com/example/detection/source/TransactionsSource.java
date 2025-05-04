package com.example.detection.source;

import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.connector.kafka.source.KafkaSource;
import org.apache.flink.connector.kafka.source.enumerator.initializer.OffsetsInitializer;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.util.ParameterTool;

import com.example.detection.deserializer.EventDeserializer;
import com.example.detection.helper.KafkaHelper;
import com.example.detection.model.Event;
import com.example.detection.model.Transaction;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * TransactionsSource:
 * Tạo Kafka source, decode Event → wrap thành Transaction
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class TransactionsSource {
    public static DataStream<Transaction> create(StreamExecutionEnvironment env, ParameterTool params) {
        String brokers = params.get("kafka.source.bootstrap.servers", "localhost:9092");
        String topic = params.get("kafka.topic.event", "event-topic");
        String groupId = params.get("kafka.source.group.id", "detection-consumer");

        // Create topic if it doesn't exist
        KafkaHelper.createTopicIfNotExists(brokers, topic, groupId, 1, (short) 1);

        KafkaSource<Event> kafkaSource = KafkaSource.<Event>builder()
                .setProperty("partition.discovery.interval.ms", "10000")
                .setBootstrapServers(brokers)
                .setTopics(topic)
                .setGroupId(groupId)
                .setStartingOffsets(OffsetsInitializer.latest())
                .setValueOnlyDeserializer(new EventDeserializer())
                .build();

        return env
                .fromSource(kafkaSource, WatermarkStrategy.noWatermarks(), "Kafka-Event-Source")
                .map(new TransactionMapper())
                .name("Event → Transaction Mapper");

    }

}
