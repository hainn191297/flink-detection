package com.example.detection.constants;

import java.util.List;

import org.apache.flink.api.common.state.MapStateDescriptor;
import org.apache.flink.api.common.typeinfo.TypeHint;
import org.apache.flink.api.common.typeinfo.TypeInformation;

import com.example.detection.model.Alert;
import com.example.detection.model.Rule;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)

public class MapStateDescriptors {
    public static final MapStateDescriptor<Long, List<Alert>> ALERTS_DESCRIPTOR = new MapStateDescriptor<>(
            "recentAlertsMap",
            TypeInformation.of(Long.class),
            TypeInformation.of(new TypeHint<List<Alert>>() {
            }));

    public static final MapStateDescriptor<String, Rule> BROADCAST_RULES_DESCRIPTOR = new MapStateDescriptor<>(
            "rulesState",
            String.class,
            Rule.class);
}
