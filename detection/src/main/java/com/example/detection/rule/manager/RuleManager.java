package com.example.detection.rule.manager;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.flink.api.common.state.BroadcastState;
import org.apache.flink.util.ParameterTool;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.example.detection.drools.DroolsEngine;
import com.example.detection.drools.DroolsEngineProvider;
import com.example.detection.model.Rule;
import com.example.detection.rule.builder.RuleDrlBuilder;
import com.example.detection.rule.loader.RedisRuleLoader;

public class RuleManager {
    private static final Logger log = LogManager.getLogger(RuleManager.class);
    private static RuleManager instance;

    private final RedisRuleLoader ruleLoader;
    private final Map<String, Rule> ruleCache;
    private final DroolsEngine engine;

    private RuleManager(ParameterTool params) {
        this.ruleLoader = RedisRuleLoader.getInstance(params);
        this.ruleCache = new ConcurrentHashMap<>();
        this.engine = DroolsEngineProvider.getInstance();

        try {
            initializeRules();
        } catch (Exception e) {
            log.error("Failed to initialize rules: {}", e.getMessage(), e);
        }
    }

    public static synchronized RuleManager getInstance(ParameterTool params) {
        if (instance == null) {
            instance = new RuleManager(params);
        }
        return instance;
    }

    private void initializeRules() {
        Map<String, Rule> initialRules = ruleLoader.loadAll();
        ruleCache.putAll(initialRules);
        engine.reloadFromDRL(RuleDrlBuilder.buildMany(initialRules));
        log.info("Initialized rule engine with {} rules", initialRules.size());
    }

    public Optional<Rule> getRule(String ruleId) {
        Rule rule = ruleCache.get(ruleId);
        if (rule == null) {
            rule = ruleLoader.getRule(ruleId);
            if (rule != null) {
                ruleCache.put(ruleId, rule);
                log.debug("Rule {} loaded from Redis and added to cache", ruleId);
            }
        }
        return Optional.ofNullable(rule);
    }

    public void addOrUpdateRule(String ruleId) {
        Rule rule = ruleLoader.getRule(ruleId);
        if (rule != null) {
            ruleCache.put(ruleId, rule);
            reloadRuleEngine();
            log.info("Rule {} added/updated in cache", ruleId);
        } else {
            log.warn("Failed to load rule {} from Redis", ruleId);
        }
    }

    public void removeRule(String ruleId) {
        ruleCache.remove(ruleId);
        reloadRuleEngine();
        log.info("Rule {} removed from cache", ruleId);
    }

    public void reloadRuleEngine() {
        engine.reloadFromDRL(RuleDrlBuilder.buildMany(ruleCache));
        log.debug("Rule engine reloaded with {} rules", ruleCache.size());
    }

    public void synchronizeWithBroadcastState(BroadcastState<String, Rule> state) throws Exception {
        // Clear cache and reload from broadcast state
        Map<String, Rule> stateRules = new ConcurrentHashMap<>();
        for (Map.Entry<String, Rule> entry : state.immutableEntries()) {
            stateRules.put(entry.getKey(), entry.getValue());
        }

        ruleCache.clear();
        ruleCache.putAll(stateRules);
        reloadRuleEngine();
        log.debug("Rule cache synchronized with {} rules from broadcast state", stateRules.size());
    }

    public void updateBroadcastState(BroadcastState<String, Rule> state, String ruleId, Rule rule) throws Exception {
        if (rule != null) {
            state.put(ruleId, rule);
        } else {
            state.remove(ruleId);
        }
    }

    public DroolsEngine getEngine() {
        return engine;
    }

    public Map<String, Rule> getRuleCache() {
        return new ConcurrentHashMap<>(ruleCache);
    }
}