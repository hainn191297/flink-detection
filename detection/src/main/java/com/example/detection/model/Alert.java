package com.example.detection.model;

import java.io.Serializable;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class Alert implements Serializable {
    private static final long serialVersionUID = 1L;

    /** Thông tin event gốc */
    private List<Event> events;

    /** Rule match */
    private Rule rule;

    private String subRuleID;

    private long eventTimestamp;

    /** Mốc thời gian hệ thống phát hiện */
    private long ingestTimestamp;
}
