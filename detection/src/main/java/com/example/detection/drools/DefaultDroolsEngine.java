package com.example.detection.drools;

import java.io.InputStream;
import java.io.StringReader;
import java.util.Collections;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kie.api.KieBase;
import org.kie.api.KieServices;
import org.kie.api.builder.KieBuilder;
import org.kie.api.builder.KieFileSystem;
import org.kie.api.builder.Message;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;

import com.example.detection.exception.DroolsCompilationException;
import com.example.detection.model.DroolsEvent;

public class DefaultDroolsEngine implements DroolsEngine {
    private static final Logger log = LogManager.getLogger(DefaultDroolsEngine.class);
    private static final String DEFAULT_RULES_FILE = "rules.drl";
    private KieBase kieBase;
    // Standard header to be prepended to all rule content
    private static final String RULE_HEADER = """
            package com.example.detection.rules;
                    import com.example.detection.model.DroolsEvent;
                    """;

    public DefaultDroolsEngine() {
        loadRulesFromFile();
    }

    private void loadRulesFromFile() {
        KieServices kieServices = KieServices.Factory.get();
        KieFileSystem kfs = kieServices.newKieFileSystem();

        try {
            // First try to load from classpath
            try (InputStream is = getClass().getResourceAsStream("/" + DEFAULT_RULES_FILE)) {
                if (is == null) {
                    log.warn("Rules file not found in classpath: {}", DEFAULT_RULES_FILE);
                    // Initialize with empty ruleset
                    reloadFromDRL("");
                    return;
                }

                // Add the rules from classpath
                kfs.write("src/main/resources/rules.drl",
                        kieServices.getResources().newInputStreamResource(is));

                KieBuilder kieBuilder = kieServices.newKieBuilder(kfs).buildAll();
                if (kieBuilder.getResults().hasMessages(Message.Level.ERROR)) {
                    log.error("Rule compilation errors: {}", kieBuilder.getResults());
                    throw new DroolsCompilationException("Failed to compile rules: " + kieBuilder.getResults());
                }

                KieContainer kieContainer = kieServices
                        .newKieContainer(kieServices.getRepository().getDefaultReleaseId());
                this.kieBase = kieContainer.getKieBase();

                int ruleCount = kieBase.getKiePackages().stream()
                        .mapToInt(pkg -> pkg.getRules().size())
                        .sum();
                log.info("Successfully loaded rules from classpath. Total rules loaded: {}", ruleCount);
            }
        } catch (Exception e) {
            log.error("Failed to load rules from classpath", e);
            reloadFromDRL("");
        }
    }

    @Override
    public synchronized void reloadFromDRL(String drl) {
        try {
            KieServices kieServices = KieServices.Factory.get();
            KieFileSystem kfs = kieServices.newKieFileSystem();

            // Ensure the DRL has the required headers with globals declaration
            String completeDrl = RULE_HEADER;
            if (!drl.trim().isEmpty()) {
                // The DRL already has the global declaration
                completeDrl = drl;
            }
            // Add the DRL content to the KieFileSystem
            kfs.write("src/main/resources/rules.drl", kieServices.getResources().newReaderResource(
                    new StringReader(completeDrl)));

            // Build the rules
            KieBuilder kieBuilder = kieServices.newKieBuilder(kfs).buildAll();
            if (kieBuilder.getResults().hasMessages(Message.Level.ERROR)) {
                log.error("Rule compilation errors: {}", kieBuilder.getResults());
                return;
            }

            // Create a new KieContainer and get the KieBase
            KieContainer kieContainer = kieServices.newKieContainer(kieServices.getRepository().getDefaultReleaseId());
            this.kieBase = kieContainer.getKieBase();

        } catch (Exception e) {
            log.error("Failed to reload rules", e);
        }
    }

    @Override
    public List<String> evaluate(DroolsEvent event) {
        if (kieBase == null) {
            log.warn("DroolsEngine is not initialized yet");
            return Collections.emptyList();
        }

        try (KieSession session = kieBase.newKieSession()) {
            session.insert(event);
            session.fireAllRules();
            session.dispose();
        } catch (Exception e) {
            log.error("Rule evaluation failed", e);
            return Collections.emptyList();
        }

        log.info("Matching rules: {}", event.getMatchingRuleIDs());
        return event.getMatchingRuleIDs();
    }

}