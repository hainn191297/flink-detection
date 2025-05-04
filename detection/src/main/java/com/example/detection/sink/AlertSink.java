package com.example.detection.sink;

import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.util.ParameterTool;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.example.detection.model.Alert;

/**
 * AlertOutSink:
 * Gửi các AlertOut (vi phạm Rule) ra Kafka dưới dạng JSON
 */
public class AlertSink extends BaseKafkaSink {
    private AlertSink() {
    }

    private static final Logger log = LogManager.getLogger(AlertSink.class);

    public static void sink(DataStream<Alert> alerts, ParameterTool params) {
        String topic = params.get("kafka.topic.alert", "alert-topic");
        String brokers = getKafkaBrokers(params);

        // Convert AlertOut → JSON and send to Kafka
        toJsonStream(alerts, "Serialize Alert to JSON")
                .sinkTo(createKafkaSink(brokers, topic, topic))
                .name("Kafka Alert Sink");

        log.info("AlertSink initialized — topic: {}, brokers: {}", topic, brokers);
    }
}
