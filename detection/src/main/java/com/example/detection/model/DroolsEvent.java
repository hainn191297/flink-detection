package com.example.detection.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class DroolsEvent implements Serializable {
    private static final long serialVersionUID = 1L;

    private Event event;

    private List<String> matchingRuleIDs = new ArrayList<>();

    public void add(String ruleID) {
        this.matchingRuleIDs.add(ruleID);
    }

}
