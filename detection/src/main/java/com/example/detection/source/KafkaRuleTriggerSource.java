package com.example.detection.source;

import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.connector.kafka.source.KafkaSource;
import org.apache.flink.connector.kafka.source.enumerator.initializer.OffsetsInitializer;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.util.ParameterTool;

import com.example.detection.deserializer.RuleTriggerEventDeserializationSchema;
import com.example.detection.helper.KafkaHelper;
import com.example.detection.model.RuleTriggerEvent;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * 📡 KafkaRuleTriggerSource
 * Đọc RuleTriggerEvent từ Kafka topic
 * Trả về DataStream<RuleTriggerEvent> cho broadcast vào RuleBroadcastFunction
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class KafkaRuleTriggerSource {

    /**
     * Tạo Kafka Flink consumer đọc RuleTriggerEvent
     *
     * @param env    Flink environment
     * @param params Thông số từ ParameterTool (chứa kafka configs)
     * @return stream of RuleTriggerEvent
     */
    public static DataStream<RuleTriggerEvent> create(StreamExecutionEnvironment env, ParameterTool params) {
        String topic = params.get("rule.trigger.kafka.topic", "rule-trigger");
        String groupId = params.get("rule.trigger.kafka.group.id", "fms-rule-trigger");
        String bootstrapServers = params.get("kafka.rule.source.bootstrap.servers", "localhost:9092");

        // Create topic if it doesn't exist
        KafkaHelper.createTopicIfNotExists(bootstrapServers, topic, groupId, 1, (short) 1);

        // KafkaSource Configuration
        KafkaSource<RuleTriggerEvent> kafkaSource = KafkaSource.<RuleTriggerEvent>builder()
                .setProperty("partition.discovery.interval.ms", "10000")
                .setBootstrapServers(bootstrapServers)
                .setTopics(topic)
                .setGroupId(groupId)
                .setStartingOffsets(OffsetsInitializer
                        .latest())
                .setDeserializer(new RuleTriggerEventDeserializationSchema())
                .build();

        return env.fromSource(kafkaSource, WatermarkStrategy.noWatermarks(), "Kafka Rule Trigger Source");
    }

}