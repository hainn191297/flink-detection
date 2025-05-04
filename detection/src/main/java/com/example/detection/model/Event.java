package com.example.detection.model;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class Event implements Serializable {
    private static final long serialVersionUID = 1L;

    @JsonProperty("AREA_ID")
    private int areaId;

    @JsonProperty("VENDOR_NAME")
    private String vendorName;

    @JsonProperty("MODIFIED_BY")
    private String modifiedBy;

    @JsonProperty("DETAILS")
    private String details;

    @JsonProperty("CLEARED_TIME")
    private Long clearedTime;

    @JsonProperty("CUSTOM_EVENT_TYPE_NAME")
    private String customEventTypeName;

    @JsonProperty("SEVERITY_LEVEL")
    private int severityLevel;

    @JsonProperty("MODIFIED_TIME")
    private Long modifiedTime;

    @JsonProperty("STATUS")
    private String status;

    @JsonProperty("PROBABLE_CAUSE")
    private String probableCause;

    @JsonProperty("SPECIFIC_PROBLEM")
    private String specificProblem;

    @JsonProperty("NOTE")
    private String note;

    @JsonProperty("NODE_DEVICE_ID")
    private String nodeDeviceId;

    @JsonProperty("NODE_TYPE_NAME")
    private String nodeTypeName;

    @JsonProperty("ERI_ALARM_ADDITIONAL_INFO")
    private String eriAlarmAdditionalInfo;

    @JsonProperty("PROBABLE_CAUSE_ID")
    private Integer probableCauseId;

    @JsonProperty("SEVERITY_LEVEL_DESC")
    private String severityLevelDesc;

    @JsonProperty("ID")
    private long id;

    @JsonProperty("TYPE")
    private int type;

    @JsonProperty("CREATED_BY")
    private String createdBy;

    @JsonProperty("CLEARED_BY")
    private String clearedBy;

    @JsonProperty("CREATED_TIME")
    private long createdTime;

    @JsonProperty("AREA_NAME")
    private String areaName;

    @JsonProperty("PROTOCOL")
    private int protocol;

    @JsonProperty("NODE_TYPE_ID")
    private int nodeTypeId;

    @JsonProperty("OBJECT")
    private String object;

    @JsonProperty("CUSTOM_EVENT_TYPE_ID")
    private Integer customEventTypeId;

    @JsonProperty("VENDOR_ID")
    private int vendorId;

    @JsonProperty("NODE_TYPE_GROUP_ID")
    private int nodeTypeGroupId;

    @JsonProperty("NODE_TYPE_GROUP_NAME")
    private String nodeTypeGroupName;

    @JsonProperty("PROTOCOL_DESC")
    private String protocolDesc;

    @JsonProperty("PING_TIME")
    private Long pingTime;

    @JsonProperty("NODE_NAME")
    private String nodeName;

    @JsonProperty("SYSTEM_EVENT_TYPE_NAME")
    private String systemEventTypeName;

    @JsonProperty("DURATION")
    private Long duration;

    @JsonProperty("SYSTEM_EVENT_TYPE_ID")
    private Integer systemEventTypeId;

    @JsonProperty("TYPE_DESC")
    private String typeDesc;

}
