package com.example.detection.model;

import java.io.Serializable;

import com.example.detection.enums.RuleAction;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * RuleTriggerEvent:
 * Đại diện cho một sự kiện trigger thay đổi rule (Kafka)
 * ex: {
 * "rule_id": "123",
 * "action": "update"
 * }
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RuleTriggerEvent implements Serializable {
    private static final long serialVersionUID = 1L;

    @JsonProperty("rule_id")
    private String ruleId;

    @JsonProperty("action")
    private RuleAction action; // ADD / UPDATE / DELETE

}
