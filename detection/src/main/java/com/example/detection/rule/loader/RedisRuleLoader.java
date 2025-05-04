package com.example.detection.rule.loader;

import java.util.HashMap;
import java.util.Map;

import org.apache.flink.util.ParameterTool;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.example.detection.exception.RedisRuleLoadException;
import com.example.detection.model.Rule;
import com.example.detection.rule.model.RuleParser;

import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.params.ScanParams;
import redis.clients.jedis.resps.ScanResult;

@Data
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class RedisRuleLoader {
    private static final Logger log = LogManager.getLogger(RedisRuleLoader.class);

    private static RedisRuleLoader instance;

    private String host;
    private int port;
    private String password;
    private JedisPool jedisPool;
    private String keyPrefix;

    /**
     * Initialize the singleton instance with parameters
     *
     * @param params Flink ParameterTool containing Redis configuration
     * @return The singleton instance of RedisRuleLoader
     */
    public static synchronized RedisRuleLoader getInstance(ParameterTool params) {
        if (instance == null) {
            instance = new RedisRuleLoader();
            instance.host = params.get("redis.host", "localhost");
            instance.port = params.getInt("redis.port", 6379);
            instance.password = params.get("redis.password", "");
            instance.keyPrefix = params.get("redis.key.prefix", "rules:");

            JedisPoolConfig poolConfig = new JedisPoolConfig();
            instance.jedisPool = new JedisPool(poolConfig, instance.host, instance.port, 2000, instance.password);

            log.info("RedisRuleLoader initialized with host: {}, port: {}, keyPrefix: {}",
                    instance.host, instance.port, instance.keyPrefix);
        }
        return instance;
    }

    /**
     * Load toàn bộ Rule từ Redis (rules:<id>)
     * Nếu gặp lỗi → dừng job
     */
    public Map<String, Rule> loadAll() {
        Map<String, Rule> rules = new HashMap<>();
        try (Jedis jedis = jedisPool.getResource()) {
            String cursor = ScanParams.SCAN_POINTER_START;
            ScanParams scanParams = new ScanParams().match(keyPrefix + "*").count(1000);

            do {
                ScanResult<String> result = jedis.scan(cursor, scanParams);
                for (String key : result.getResult()) {
                    String json = jedis.get(key);
                    if (json != null && !json.isBlank()) {
                        parseRuleFromJson(key, json, rules);
                    }
                }
                cursor = result.getCursor();
            } while (!cursor.equals(ScanParams.SCAN_POINTER_START));
            log.info("Loaded {} rules from Redis", rules.size());
            return rules;

        } catch (Exception e) {
            log.error("Critical error loading rules from Redis: {}", e.getMessage(), e);
            throw new RedisRuleLoadException("Failed to load rules from Redis", e);
        }
    }

    private void parseRuleFromJson(String key, String json, Map<String, Rule> rules) {
        try {
            Rule model = RuleParser.parse(json);
            String id = key.replaceFirst(keyPrefix, "");
            rules.put(id, model);
        } catch (Exception e) {
            log.error("Failed to parse rule JSON from Redis key {}: {}", key, e.getMessage());
            throw new RedisRuleLoadException("Invalid rule data in Redis", e);
        }
    }

    /**
     * Get 1 rule by ID (logs warning, does not crash job)
     */
    public Rule getRule(String id) {
        try (Jedis jedis = jedisPool.getResource()) {
            String json = jedis.get(keyPrefix + id);
            if (json == null || json.isBlank()) {
                log.warn("Rule with id {} not found in Redis", id);
                return null;
            }
            return RuleParser.parse(json);
        } catch (Exception e) {
            log.error("Failed to get rule {} from Redis: {}", id, e.getMessage());
            return null;
        }
    }

    /**
     * Save a rule to Redis
     */
    public void saveRule(Rule rule) {
        try (Jedis jedis = jedisPool.getResource()) {
            String json = RuleParser.parse(rule);
            jedis.set(keyPrefix + rule.getId(), json);
        } catch (Exception e) {
            log.error("Failed to save rule {} to Redis: {}", rule.getId(), e.getMessage());
            throw new RedisRuleLoadException("Failed to save rule to Redis", e);
        }
    }
}