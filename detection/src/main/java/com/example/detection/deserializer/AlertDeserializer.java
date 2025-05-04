package com.example.detection.deserializer;

import java.io.IOException;
import java.util.ArrayList;

import org.apache.flink.api.common.serialization.DeserializationSchema;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.example.detection.model.Alert;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * AlertDeserializer:
 * Kafka byte[] → Alert (Jackson)
 */
public class AlertDeserializer implements DeserializationSchema<Alert> {

    private static final Logger log = LogManager.getLogger(AlertDeserializer.class);
    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    public Alert deserialize(byte[] message) throws IOException {
        try {
            Alert alert = mapper.readValue(message, Alert.class);
            if (alert.getEvents() != null && !(alert.getEvents() instanceof ArrayList)) {
                alert.setEvents(new ArrayList<>(alert.getEvents()));
            }

            return alert;
        } catch (Exception e) {
            log.error("Error parsing Alert JSON: {}", new String(message), e);
            return null;
        }
    }

    @Override
    public boolean isEndOfStream(Alert nextElement) {
        return false;
    }

    @Override
    public TypeInformation<Alert> getProducedType() {
        return TypeInformation.of(Alert.class);
    }
}
