package com.example.detection;


import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.datastream.BroadcastStream;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.util.ParameterTool;

import com.example.detection.config.DetectionProperties;
import com.example.detection.constants.MapStateDescriptors;
import com.example.detection.constants.OutputTags;
import com.example.detection.functions.DynamicDroolsAlertFunction;
import com.example.detection.model.Alert;
import com.example.detection.model.RuleTriggerEvent;
import com.example.detection.model.Transaction;
import com.example.detection.sink.AlertSink;
import com.example.detection.sink.LatencySink;
import com.example.detection.source.KafkaRuleTriggerSource;
import com.example.detection.source.TransactionsSource;
import com.example.detection.utils.StreamLogger;

//@SpringBootApplication
public class DetectionApplication {

    public static void main(String[] args) throws Exception {
        // Load config
        ParameterTool params = DetectionProperties.load(args);

        Configuration config = new Configuration();
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment(config);
        env.getConfig().setGlobalJobParameters(params);

        // Rule broadcast from Kafka → Flink
        BroadcastStream<RuleTriggerEvent> ruleBroadcastStream = KafkaRuleTriggerSource
                .create(env, params)
                .broadcast(MapStateDescriptors.BROADCAST_RULES_DESCRIPTOR);

        // Transaction Stream
        DataStream<Transaction> txStream = TransactionsSource.create(env, params);
        // Log transaction events with detailed information
        txStream = StreamLogger.logTransactionStream(txStream);

        // Apply Rules
        DataStream<Alert> alerts = txStream
                .connect(ruleBroadcastStream)
                .process(new DynamicDroolsAlertFunction())
                .name("Dynamic Drools Evaluator");

        StreamLogger.logAlertStream(alerts);

        AlertSink.sink(alerts, params);

        // Log alert events with detailed information
        SingleOutputStreamOperator<Alert> alertStream = (SingleOutputStreamOperator<Alert>) alerts;
        DataStream<Long> latency = alertStream.getSideOutput(OutputTags.LATENCY_SINK_TAG);
        LatencySink.sink(latency, params);

        // Start job
        env.execute("FMS Detection Engine");
    }

}
