package com.example.detection.rule.runtime;

import java.util.HashMap;
import java.util.Map;

import org.apache.flink.api.common.state.MapState;

import com.example.detection.model.Rule;

/**
 * RuleCache:
 * Thao tác state chứa các rule hiện hành
 */
public class RuleCache {

    private final MapState<String, Rule> state;

    public RuleCache(MapState<String, Rule> state) {
        this.state = state;
    }

    public void put(String id, Rule rule) throws Exception {
        state.put(id, rule);
    }

    public void remove(String id) throws Exception {
        state.remove(id);
    }

    public Map<String, Rule> snapshot() throws Exception {
        Map<String, Rule> result = new HashMap<>();
        for (Map.Entry<String, Rule> entry : state.entries()) {
            result.put(entry.getKey(), entry.getValue());
        }
        return result;
    }
}
