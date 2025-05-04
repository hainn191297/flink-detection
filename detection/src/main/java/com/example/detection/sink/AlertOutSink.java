package com.example.detection.sink;

import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.util.ParameterTool;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.example.detection.model.Alert;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * AlertOutSink:
 * Gửi các AlertOut (vi phạm Rule) ra Kafka dưới dạng JSON
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class AlertOutSink extends BaseKafkaSink {
    private static final Logger log = LogManager.getLogger(AlertOutSink.class);

    public static void sink(DataStream<Alert> alerts, ParameterTool params) {
        String topic = params.get("kafka.topic.alert.complex", "alert-complex-topic");
        String brokers = getKafkaBrokers(params);

        // Convert AlertOut → JSON and send to Kafka
        toJsonStream(alerts, "Serialize Alert Complex to JSON")
                .sinkTo(createKafkaSink(brokers, topic, topic))
                .name("Kafka Alert Complex Sink");

        log.info("Alert Complex Sink initialized — topic: {}, brokers: {}", topic, brokers);
    }
}
