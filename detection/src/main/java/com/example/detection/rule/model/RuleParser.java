package com.example.detection.rule.model;

import com.example.detection.exception.RuleParseException;
import com.example.detection.model.Rule;
import com.fasterxml.jackson.databind.ObjectMapper;

// define rule model parser
public class RuleParser {

    private static final ObjectMapper mapper = new ObjectMapper();

    private RuleParser() {
    }

    public static Rule parse(String jsonString) {
        try {
            return mapper.readValue(jsonString, Rule.class);
        } catch (Exception e) {
            throw new RuleParseException("Failed to parse Rule JSON", e);
        }
    }

    public static String parse(Rule rule) {
        try {
            return mapper.writeValueAsString(rule);
        } catch (Exception e) {
            throw new RuleParseException("Failed to convert Rule to JSON", e);
        }
    }

}
