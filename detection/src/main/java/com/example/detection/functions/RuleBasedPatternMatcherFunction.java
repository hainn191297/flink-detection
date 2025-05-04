package com.example.detection.functions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.flink.api.common.functions.OpenContext;
import org.apache.flink.api.common.functions.RichFlatMapFunction;
import org.apache.flink.api.common.state.MapState;
import org.apache.flink.api.common.state.MapStateDescriptor;
import org.apache.flink.util.Collector;

import com.example.detection.constants.MapStateDescriptors;
import com.example.detection.model.Alert;
import com.example.detection.model.Event;
import com.example.detection.model.Rule;
import com.example.detection.model.SubRule;

public class RuleBasedPatternMatcherFunction extends RichFlatMapFunction<Alert, Alert> {
    // Store recent alerts indexed by ruleId -> List of alerts
    private transient MapState<Long, List<Alert>> recentAlertsMap;
    // Store rule metadata
    private transient Map<Long, Rule> rulesMap;

    @Override
    public void open(OpenContext openContext) {
        MapStateDescriptor<Long, List<Alert>> descriptor = MapStateDescriptors.ALERTS_DESCRIPTOR;

        recentAlertsMap = getRuntimeContext().getMapState(descriptor);
        rulesMap = new HashMap<>();
    }

    @Override
    public void flatMap(Alert alert, Collector<Alert> out) throws Exception {

        Rule rule = alert.getRule();
        Long ruleId = rule.getId();
        // Store rule metadata
        rulesMap.putIfAbsent(ruleId, rule);

        // Only process active, windowed rules
        if (!rule.isActive() || !rule.isWindowed()) {
            return;
        }

        // Get or create list for this rule
        List<Alert> ruleAlerts = recentAlertsMap.get(ruleId);
        if (ruleAlerts == null) {
            ruleAlerts = new ArrayList<>();
        }
        recentAlertsMap.put(ruleId, ruleAlerts);
        // Add current alert
        ruleAlerts.add(alert);

        // Remove old alerts outside the time window
        long windowMillis = 60L * 1000 * rule.getInterval();
        long oldestTimestampToKeep = alert.getEventTimestamp() - windowMillis;
        ruleAlerts.removeIf(a -> a.getEventTimestamp() < oldestTimestampToKeep);

        // Check if all subRules are matched within the time window
        Set<String> matchedSubRuleIds = ruleAlerts.stream()
                .map(Alert::getSubRuleID)
                .collect(Collectors.toSet());

        // Get all expected subRuleIDs from the rule
        Set<String> allSubRuleIds = rule.getSubQueries().stream()
                .map(SubRule::getId)
                .collect(Collectors.toSet());

        // Check if all subRules have been matched
        if (matchedSubRuleIds.containsAll(allSubRuleIds)) {
            // Find the latest timestamp
            long latestTimestamp = ruleAlerts.stream()
                    .mapToLong(Alert::getEventTimestamp)
                    .max()
                    .orElse(0);

            // Find the earliest timestamp for matched alerts
            long earliestTimestamp = ruleAlerts.stream()
                    .mapToLong(Alert::getEventTimestamp)
                    .min()
                    .orElse(0);

            // Only create a composite alert if all events are within the window
            if ((latestTimestamp - earliestTimestamp) <= windowMillis) {
                // Create new composite alert
                Alert compositeAlert = new Alert();
                compositeAlert.setRule(rule);
                compositeAlert.setSubRuleID("ALL_MATCHED");
                compositeAlert.setEventTimestamp(latestTimestamp);
                compositeAlert.setIngestTimestamp(System.currentTimeMillis());

                // Collect events from all alerts that contributed to the match
                // Create a map to ensure we get one alert per subRuleID (latest one)
                Map<String, Alert> uniqueSubRuleAlerts = new HashMap<>();
                for (Alert a : ruleAlerts) {
                    if (allSubRuleIds.contains(a.getSubRuleID())) {
                        uniqueSubRuleAlerts.put(a.getSubRuleID(), a);
                    }
                }

                // Collect all events from the unique alerts
                ArrayList<Event> allEvents = uniqueSubRuleAlerts.values()
                        .stream()
                        .map(x -> x.getEvents().get(0))
                        .collect(Collectors.toCollection(ArrayList::new));

                compositeAlert.setEvents(allEvents);

                // Output the composite alert
                out.collect(compositeAlert);

                // Clear the matched alerts to avoid duplicate matches
                // or keep them if you want overlapping matches
                ruleAlerts.clear();
            }
        }
    }

    @Override
    public void close() {
        if (recentAlertsMap != null) {
            recentAlertsMap.clear();
        }
        if (rulesMap != null) {
            rulesMap.clear();
        }
    }
}