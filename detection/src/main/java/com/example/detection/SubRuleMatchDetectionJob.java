package com.example.detection;

import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.util.ParameterTool;

import com.example.detection.config.DetectionProperties;
import com.example.detection.functions.RuleBasedPatternMatcherFunction;
import com.example.detection.model.Alert;
import com.example.detection.sink.AlertOutSink;
import com.example.detection.source.AlertsSource;

public class SubRuleMatchDetectionJob {

    public static void main(String[] args) throws Exception {
        // Load parameters
        ParameterTool params = DetectionProperties.load(args);

        // Configure environment
        Configuration config = new Configuration();

        // Create execution environment
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment(config);
        env.getConfig().setGlobalJobParameters(params);

        // Create alert source stream
        DataStream<Alert> alertStream = AlertsSource.create(env, params);

        // Apply CEP pattern to detect when all subRules of a rule are matched
        DataStream<Alert> compositeAlertStream = alertStream.keyBy(alert -> alert.getRule().getId())
                .flatMap(new RuleBasedPatternMatcherFunction());

        // Process the composite alerts (e.g., send to Kafka, store in database)
        // For example:
        AlertOutSink.sink(compositeAlertStream, params);

        // Execute the job
        env.execute("All SubRules Matched Detection Job");
    }
}