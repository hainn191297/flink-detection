package com.example.detection;

import java.util.Properties;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class KafkaJsonProducer {
    private static final Logger log = LogManager.getLogger(KafkaJsonProducer.class);

    private static final int TARGET_MESSAGES_PER_SECOND = 10000;
    private static final AtomicLong messageCounter = new AtomicLong(0);
    private static final AtomicLong lastLogTime = new AtomicLong(System.currentTimeMillis());

    public static void main(String[] args) {
        // Kafka Broker
        String bootstrapServers = "kafka:9092"; // Update if needed
        String topic = "event-topic"; // Update if needed

        // Kafka Producer Properties
        Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.put(ProducerConfig.BATCH_SIZE_CONFIG, 16384);
        props.put(ProducerConfig.LINGER_MS_CONFIG, 1);
        props.put(ProducerConfig.BUFFER_MEMORY_CONFIG, 33554432);
        props.put(ProducerConfig.COMPRESSION_TYPE_CONFIG, "snappy");

        // Example event JSON that matches rule 1
        String exampleEventJson1 = """
                {
                    "AREA_ID": 1,
                    "VENDOR_NAME": "Ericsson",
                    "MODIFIED_BY": null,
                    "DETAILS": "The IP path local IP 10.201.72.137 - remote IP 10.221.88.141 has changed its state to INACTIVE. AssocId is 314605 EqPos 2.1, InstId 1. UlpKey is 0x20101e81: HiWord =8208, LoWord(Remote SP)=314605.",
                    "CLEARED_TIME": null,
                    "CUSTOM_EVENT_TYPE_NAME": null,
                    "SEVERITY_LEVEL": 3,
                    "MODIFIED_TIME": null,
                    "STATUS": null,
                    "PROBABLE_CAUSE": "linkDown",
                    "SPECIFIC_PROBLEM": "sctpIPPathFailure",
                    "NOTE": null,
                    "NODE_DEVICE_ID": 5,
                    "NODE_TYPE_NAME": "MME",
                    "ERI_ALARM_ADDITIONAL_INFO": "2",
                    "PROBABLE_CAUSE_ID": null,
                    "SEVERITY_LEVEL_DESC": null,
                    "ID": 1,
                    "TYPE": 1,
                    "CREATED_BY": null,
                    "CLEARED_BY": null,
                    "CREATED_TIME": 1744874416000,
                    "AREA_NAME": "Khu vực 1,8640199",
                    "PROTOCOL": 0,
                    "NODE_TYPE_ID": 1,
                    "OBJECT": "SubNetwork=HNI,ManagedElement=MMEE2E,SgsnMme=1,PIU=2.1,FaultId=314605",
                    "CUSTOM_EVENT_TYPE_ID": null,
                    "VENDOR_ID": 1,
                    "NODE_TYPE_GROUP_ID": 1,
                    "NODE_TYPE_GROUP_NAME": "HLR",
                    "PROTOCOL_DESC": null,
                    "PING_TIME": null,
                    "NODE_NAME": "MMEE2E",
                    "SYSTEM_EVENT_TYPE_NAME": null,
                    "DURATION": null,
                    "SYSTEM_EVENT_TYPE_ID": null,
                    "TYPE_DESC": null
                }
                """;
        String exampleEventJson2 = """
                {
                    "AREA_ID": 1,
                    "VENDOR_NAME": "Ericsson",
                    "MODIFIED_BY": null,
                    "DETAILS": "The IP path local IP 10.201.72.137 - remote IP 10.221.88.141 has taolaphat its state to INACTIVE. AssocId is 314605 EqPos 2.1, InstId 1. UlpKey is 0x20101e81: HiWord =8208, LoWord(Remote SP)=314605,taolaphat.",
                    "CLEARED_TIME": null,
                    "CUSTOM_EVENT_TYPE_NAME": null,
                    "SEVERITY_LEVEL": 3,
                    "MODIFIED_TIME": null,
                    "STATUS": null,
                    "PROBABLE_CAUSE": "linkDown",
                    "SPECIFIC_PROBLEM": "sctpIPPathFailure",
                    "NOTE": null,
                    "NODE_DEVICE_ID": 5,
                    "NODE_TYPE_NAME": "MME",
                    "ERI_ALARM_ADDITIONAL_INFO": "2",
                    "PROBABLE_CAUSE_ID": null,
                    "SEVERITY_LEVEL_DESC": null,
                    "ID": 2,
                    "TYPE": 1,
                    "CREATED_BY": null,
                    "CLEARED_BY": null,
                    "CREATED_TIME": 1744874416000,
                    "AREA_NAME": "Khu vực 1,8640199",
                    "PROTOCOL": 0,
                    "NODE_TYPE_ID": 1,
                    "OBJECT": "SubNetwork=HNI,ManagedElement=MMEE2E,SgsnMme=1,PIU=2.1,FaultId=314605",
                    "CUSTOM_EVENT_TYPE_ID": null,
                    "VENDOR_ID": 1,
                    "NODE_TYPE_GROUP_ID": 1,
                    "NODE_TYPE_GROUP_NAME": "HLR",
                    "PROTOCOL_DESC": null,
                    "PING_TIME": null,
                    "NODE_NAME": "MMEE2E",
                    "SYSTEM_EVENT_TYPE_NAME": null,
                    "DURATION": null,
                    "SYSTEM_EVENT_TYPE_ID": null,
                    "TYPE_DESC": null
                }
                """;
        String exampleEventJson3 = """
                [{"issue3":0,"issue2":0,"issue1":5}]
                """;
        // Create Kafka Producer
        try (Producer<String, String> producer = new KafkaProducer<>(props)) {
            long messageId = 1;
            int j = 0;
            while (true) {
                j++;
                if (j == 2) { // true at Integer.MAX_VALUE +1
                    break;
                }

                // Generate unique message with current timestamp
                String jsonData1 = String.format(exampleEventJson1,
                        messageId,
                        messageId,
                        java.time.LocalDateTime.now()
                                .format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
                        messageId,
                        messageId);
                String jsonData2 = String.format(exampleEventJson2,
                        messageId,
                        messageId,
                        java.time.LocalDateTime.now()
                                .format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
                        messageId,
                        messageId);
                // Send Data to Kafka
                producer.send(new ProducerRecord<>(topic, exampleEventJson3));
                producer.send(new ProducerRecord<>(topic, jsonData1));
                producer.send(new ProducerRecord<>(topic, jsonData2));

                // Track messages per second
                long currentCount = messageCounter.incrementAndGet();
                long currentTime = System.currentTimeMillis();
                long lastTime = lastLogTime.get();

                if (currentTime - lastTime >= 1000 && lastLogTime.compareAndSet(lastTime, currentTime)) {
                    log.info("Messages sent in last second: {}", currentCount);
                    messageCounter.set(0);
                }

                messageId++;

                // Optional: Add a small delay if we're sending too fast
                if (currentCount > TARGET_MESSAGES_PER_SECOND) {
                    Thread.sleep(1);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            Thread.currentThread().interrupt();
        }
    }
}
