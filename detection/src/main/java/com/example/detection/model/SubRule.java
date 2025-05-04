package com.example.detection.model;

import java.io.Serializable;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class SubRule implements Serializable {
    private static final long serialVersionUID = 1L;

    @JsonProperty("id")
    private String id;

    @JsonProperty("nodeType")
    private int nodeType;
    @JsonProperty("nodeDeviceIds")
    private List<Integer> nodeDeviceId;

    @JsonProperty("probableCause")
    private String probableCause;

    @JsonProperty("details")
    private String details;

    @JsonProperty("severityLevel")
    private int severityLevel;

    @JsonProperty("name")
    private String name;
}
