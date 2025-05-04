package com.example.detection.functions;

import java.util.ArrayList;
import java.util.List;

import org.apache.flink.api.common.functions.OpenContext;
import org.apache.flink.api.common.state.BroadcastState;
import org.apache.flink.streaming.api.functions.co.BroadcastProcessFunction;
import org.apache.flink.util.Collector;
import org.apache.flink.util.ParameterTool;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.example.detection.constants.MapStateDescriptors;
import com.example.detection.constants.OutputTags;
import com.example.detection.drools.DroolsEngine;
import com.example.detection.model.Alert;
import com.example.detection.model.DroolsEvent;
import com.example.detection.model.Event;
import com.example.detection.model.Rule;
import com.example.detection.model.RuleTriggerEvent;
import com.example.detection.model.Transaction;
import com.example.detection.rule.manager.RuleManager;

public class DynamicDroolsAlertFunction
        extends BroadcastProcessFunction<Transaction, RuleTriggerEvent, Alert> {

    private static final Logger log = LogManager.getLogger(DynamicDroolsAlertFunction.class);
    private transient RuleManager ruleManager;
    private transient DroolsEngine engine;

    @Override
    public void open(OpenContext openContext) throws Exception {
        ParameterTool params = ParameterTool.fromMap(getRuntimeContext().getGlobalJobParameters());
        this.ruleManager = RuleManager.getInstance(params);
        this.engine = ruleManager.getEngine();
    }

    @Override
    public void processElement(Transaction tx, ReadOnlyContext ctx, Collector<Alert> out) throws Exception {
        if (ruleManager.getRuleCache().isEmpty()) {
            log.warn("No rules available to process the transaction.");
            return;
        }

        long now = System.currentTimeMillis();

        DroolsEvent droolsEvent = new DroolsEvent();
        droolsEvent.setEvent(tx.getEvent());

        List<String> matchingRules = new ArrayList<>(engine.evaluate(droolsEvent));

        for (String subRuleId : matchingRules) {
            String ruleID = subRuleId.split("-")[0];

            Rule rule = ruleManager.getRule(ruleID).orElse(null);

            if (rule == null) {
                log.warn("Matched rule {} not found in cache or Redis", ruleID);
                continue;
            }

            List<Event> events = new ArrayList<>();
            events.add(tx.getEvent());
            Alert alert = new Alert(events, rule, subRuleId, tx.getEventTimestamp(), now);
            out.collect(alert);
            ctx.output(OutputTags.LATENCY_SINK_TAG, tx.getIngestTimestamp() - tx.getEventTimestamp());
        }
    }

    @Override
    public void processBroadcastElement(RuleTriggerEvent event, Context ctx, Collector<Alert> out) throws Exception {
        String ruleId = event.getRuleId();
        BroadcastState<String, Rule> rulesState = ctx.getBroadcastState(MapStateDescriptors.BROADCAST_RULES_DESCRIPTOR);
        try {
            switch (event.getAction()) {
                case ADD, UPDATE -> {
                    ruleManager.addOrUpdateRule(ruleId);

                    Rule rule = ruleManager.getRule(ruleId).orElse(null);
                    if (rule != null) {
                        ruleManager.updateBroadcastState(rulesState, ruleId, rule);
                        log.info("Rule {} updated in broadcast state", ruleId);
                    }
                }
                case DELETE -> {
                    ruleManager.removeRule(ruleId);
                    rulesState.remove(ruleId);
                    log.info("Rule {} removed from broadcast state", ruleId);
                }
                default -> {
                    return;
                }
            }

            // Synchronize the rule manager with broadcast state
            ruleManager.synchronizeWithBroadcastState(rulesState);

        } catch (Exception e) {
            log.error("Rule broadcast processing failed: {}", e.getMessage(), e);
        }
    }
}