package com.example.detection.constants;

import org.apache.flink.api.common.typeinfo.Types;
import org.apache.flink.util.OutputTag;

import com.example.detection.model.Rule;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class OutputTags {

    public static final OutputTag<Long> LATENCY_SINK_TAG = new OutputTag<>("latency-output", Types.LONG);

    public static final OutputTag<Rule> CURRENT_RULES_SINK_TAG = new OutputTag<>("current-rules-output",
            Types.POJO(Rule.class));

    public static final OutputTag<String> RULE_RELOADED_OUTPUT = new OutputTag<>("rule-reloaded-output",
            Types.STRING);
}
