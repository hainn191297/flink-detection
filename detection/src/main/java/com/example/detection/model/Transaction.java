package com.example.detection.model;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class Transaction implements Serializable {
    private static final long serialVersionUID = 1L;

    /** Primary key dùng cho partition ví dụ key: nodeDeviceId */
    private String key;

    /** Metadata phụ trợ (area, vendor, etc.) */
    // private Map<String, String> attributes;
    private Event event;

    /** Timestamp theo event thực tế (event time) */
    private long eventTimestamp;

    /** Timestamp hệ thống (processing time) */
    private long ingestTimestamp;
}
