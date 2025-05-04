package com.example.detection.deserializer;

import java.io.IOException;

import org.apache.flink.api.common.serialization.DeserializationSchema;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.example.detection.model.Event;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * EventDeserializer:
 * Kafka byte[] → Event (Jackson)
 */
public class EventDeserializer implements DeserializationSchema<Event> {

    private static final Logger log = LogManager.getLogger(EventDeserializer.class);
    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    public Event deserialize(byte[] message) throws IOException {
        try {
            return mapper.readValue(message, Event.class);
        } catch (Exception e) {
            log.error("Error parsing Event JSON: {}", new String(message), e);
            return null;
        }
    }

    @Override
    public boolean isEndOfStream(Event nextElement) {
        return false;
    }

    @Override
    public TypeInformation<Event> getProducedType() {
        return TypeInformation.of(Event.class);
    }
}
