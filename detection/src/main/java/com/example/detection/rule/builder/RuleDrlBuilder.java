package com.example.detection.rule.builder;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.example.detection.model.Rule;
import com.example.detection.model.SubRule;

/**
 * RuleDrlBuilder:
 * Tạo DRL string từ Rule và SubRule.
 * Mỗi Rule được đặt tên bằng ID gốc từ Redis.
 */
public class RuleDrlBuilder {
    private RuleDrlBuilder() {

    }

    private static final String DRL_HEADER = """
            package com.rules;
            import com.example.detection.model.DroolsEvent;
            """;

    /**
     * Build toàn bộ DRL từ nhiều Rule (dạng Map<id, Rule>)
     */
    public static String buildMany(Map<String, Rule> rules) {
        StringBuilder builder = new StringBuilder(DRL_HEADER);
        for (Rule rule : rules.values()) {
            if (!rule.isActive() || rule.getQuery() == null || rule.getQuery().isBlank())
                continue;
            String ruleBlock = build(rule);
            if (!ruleBlock.isBlank()) {
                builder.append(ruleBlock).append("\n");
            }
        }
        return builder.toString();
    }

    /**
     * Build DRL từ một Rule đơn
     */
    public static String build(Rule rule) {
        StringBuilder completedRule = new StringBuilder();
        // Build subQuery → expression
        Map<String, String> subConditions = rule.getSubQueries().stream()
                .collect(Collectors.toMap(
                        SubRule::getId,
                        RuleDrlBuilder::buildSubCondition,
                        (a, b) -> a));

        // Thay thế ID trong query chính bằng điều kiện thực tế
        for (Map.Entry<String, String> entry : subConditions.entrySet()) {
            String condition = entry.getValue();
            completedRule.append(String.format("""
                    rule "%s"
                    when
                        $e: DroolsEvent( %s )
                    then
                        $e.add("%s");
                    end
                    """, entry.getKey(), condition, entry.getKey()));
        }

        return completedRule.toString();
    }

    private static String buildSubCondition(SubRule sub) {
        String detailExpr = parseDetailExpression(sub.getDetails());
        String probableCauseExpr = buildMatchExpression(sub.getProbableCause(), "event.getProbableCause()");
        String nodeDeviceIdExpr = buildContainExpression(sub.getNodeDeviceId());
        return String.format(
                "event.getSeverityLevel() == %d && event.getNodeTypeId() == %d && %s && %s && %s",
                sub.getSeverityLevel(), sub.getNodeType(), nodeDeviceIdExpr, probableCauseExpr, detailExpr);
    }

    private static String parseDetailExpression(String input) {
        if (input == null || input.isBlank())
            return "true";

        StringBuilder output = new StringBuilder();
        StringBuilder token = new StringBuilder();
        boolean inQuote = false;

        for (char ch : input.toCharArray()) {
            if (ch == '"') {
                inQuote = !inQuote;
                token.append(ch);
            } else if (!inQuote && (ch == '(' || ch == ')')) {
                flushToken(token, output);
                output.append(ch).append(" ");
            } else if (!inQuote && Character.isWhitespace(ch)) {
                flushToken(token, output);
            } else {
                token.append(ch);
            }
        }

        flushToken(token, output);
        return output.toString().trim();
    }

    private static void flushToken(StringBuilder token, StringBuilder output) {
        if (token.isEmpty())
            return;

        String value = token.toString().trim();
        switch (value.toUpperCase()) {
            case "AND" -> output.append("&& ");
            case "OR" -> output.append("|| ");
            case "NOT" -> output.append("!"); // ghép với chuỗi tiếp theo
            default -> {
                if (value.startsWith("\"") && value.endsWith("\"")) {
                    output.append(buildMatchExpression(value, "event.getDetails()")).append(" ");
                } else {
                    output.append(value).append(" ");
                }
            }
        }

        token.setLength(0);
    }

    private static String buildMatchExpression(String quoted, String field) {
        String content = "";
        if (quoted != null && !quoted.isBlank()) {
            content = quoted.substring(1, quoted.length() - 1)
                    .replaceAll("([\\\\.*+?\\[^\\]$(){}=!<>|:\\-])", "\\\\$1");
        }

        return String.format("%s.matches(\".*%s.*\")", field, content);
    }

    private static String buildContainExpression(List<Integer> nodeDeviceIds) {
        StringBuilder bld = new StringBuilder();
        for (Integer nodeDevice : nodeDeviceIds) {
            bld.append(String.format("event.getNodeDeviceId() == \"%s\" ||", nodeDevice));
        }
        String content = bld.toString();
        return String.format("(%s)", content.substring(0, content.length() - 2));
    }
}
