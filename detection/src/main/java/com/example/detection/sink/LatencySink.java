package com.example.detection.sink;

import java.io.Serializable;
import java.time.Duration;

import org.apache.flink.api.common.functions.AggregateFunction;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.windowing.assigners.TumblingProcessingTimeWindows;
import org.apache.flink.util.ParameterTool;

/**
 * Mục tiêu:
 * - Nhận stream độ trễ (long ms)
 * - Tính trung bình trong mỗi 10s window
 * - Gửi kết quả ra Kafka dưới dạng chuỗi JSON hoặc plain text
 */
public class LatencySink extends BaseKafkaSink {
    private LatencySink() {
    }

    public static void sink(DataStream<Long> latencyStream, ParameterTool params) {
        String brokers = getKafkaBrokers(params);
        String topic = params.get("kafka.latency.topic", "latency-topic");

        // Gom dữ liệu trong mỗi 10s theo processing time. Có thể thay đổi hoặc đưa ra
        // config với từng bài toán
        latencyStream
                .windowAll(TumblingProcessingTimeWindows.of(Duration.ofSeconds(10)))
                // Tính trung bình độ trễ
                .aggregate(new AverageLatencyAggregate())
                // Chuyển kết quả thành chuỗi
                .map(avg -> "Average Latency: " + avg + " ms")
                // Gửi ra Kafka topic
                .sinkTo(createKafkaSink(brokers, topic, topic))
                .name("Kafka Latency Sink");
    }

    /**
     * AggregateFunction để tính trung bình
     * Duy trì tổng + số lượng → cuối window tính average
     */
    private static class AverageLatencyAggregate implements AggregateFunction<Long, LatencyAccumulator, Long> {
        @Override
        public LatencyAccumulator createAccumulator() {
            return new LatencyAccumulator();
        }

        @Override
        public LatencyAccumulator add(Long value, LatencyAccumulator acc) {
            acc.count++;
            acc.total += value;
            return acc;
        }

        @Override
        public Long getResult(LatencyAccumulator acc) {
            return acc.count == 0 ? 0L : acc.total / acc.count;
        }

        @Override
        public LatencyAccumulator merge(LatencyAccumulator a, LatencyAccumulator b) {
            a.total += b.total;
            a.count += b.count;
            return a;
        }
    }

    /**
     * Bộ đệm trạng thái trung gian cho aggregate
     */
    public static class LatencyAccumulator implements Serializable {
        private static final long serialVersionUID = 1L;

        long total = 0;
        long count = 0;
    }
}
