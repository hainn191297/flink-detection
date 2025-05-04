package com.example.detection.helper;

import java.util.Collections;
import java.util.Properties;

import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class KafkaHelper {
    private static final Logger log = LogManager.getLogger(KafkaHelper.class);

    public static void createTopicIfNotExists(String bootstrapServers, String topic, String groupID, int partitions,
            short replicationFactor) {
        Properties props = new Properties();
        props.put("bootstrap.servers", bootstrapServers);
        props.put(CommonClientConfigs.GROUP_ID_CONFIG, groupID);
        props.put("client.id", "admin-client-" + topic);
        try (AdminClient adminClient = AdminClient.create(props)) {
            boolean topicExists = adminClient.listTopics().names().get().contains(topic);

            if (!topicExists) {
                NewTopic newTopic = new NewTopic(topic, partitions, replicationFactor);
                adminClient.createTopics(Collections.singleton(newTopic)).all().get();
                log.info("Topic '{}' created successfully", topic);
            }
        } catch (Exception e) {
            log.warn("Failed to create topic '{}': {}", topic, e.getMessage());
            if (e instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
        }
    }
}
