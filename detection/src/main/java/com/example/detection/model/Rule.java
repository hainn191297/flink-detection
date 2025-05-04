package com.example.detection.model;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;

import com.example.detection.enums.RuleState;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;
import lombok.Setter;

// define rule
@Data
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class Rule implements Serializable {
    private static final long serialVersionUID = 1L;

    @JsonProperty("id")
    private Long id;

    @JsonProperty("name")
    private String name;

    @JsonProperty("query")
    private String query;

    @JsonProperty("subQueries")
    private List<SubRule> subQueries;

    @JsonProperty("interval")
    private int interval; // minutes

    @JsonProperty("state")
    private RuleState state;

    public boolean isWindowed() {
        return interval > 0;
    }

    public boolean isActive() {
        return Objects.equals(this.state, RuleState.ACTIVE);
    }
}
