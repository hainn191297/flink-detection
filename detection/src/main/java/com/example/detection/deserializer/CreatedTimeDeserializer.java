package com.example.detection.deserializer;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

public class CreatedTimeDeserializer extends JsonDeserializer<Long> {

    @Override
    public Long deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        String createdTimeStr = p.getText();
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            return sdf.parse(createdTimeStr).getTime();
        } catch (ParseException e) {
            // If parsing fails, fall back to default behavior
            try {
                return Long.parseLong(createdTimeStr);
            } catch (NumberFormatException ex) {
                throw new IOException("Unable to parse created time", ex);
            }
        }
    }

}