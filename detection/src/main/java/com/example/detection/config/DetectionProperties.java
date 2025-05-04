package com.example.detection.config;

import com.example.detection.exception.ConfigurationException;
import org.apache.flink.util.ParameterTool;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;

/**
 * DetectionProperties:
 * Load cấu hình từ CLI + file application.properties
 * Ưu tiên: CLI > file
 */
public class DetectionProperties {
    private static final Logger log = LogManager.getLogger(DetectionProperties.class);

    private static final String CONFIG_FILE = "application.properties";

    private DetectionProperties() {

    }

    /**
     * Load ParameterTool từ CLI và file
     *
     * @param args từ hàm main
     * @return ParameterTool chứa config đã merge
     * @throws ConfigurationException if there are errors loading or processing the
     *                                configuration
     */
    public static ParameterTool load(String[] args) {
        try {
            // Load CLI args
            ParameterTool cliParams = ParameterTool.fromArgs(args);

            String configPath = cliParams.get("config", CONFIG_FILE);

            Properties fileProps = new Properties();
            try (InputStream is = DetectionProperties.class.getResourceAsStream("/" + configPath)) {
                if (is != null) {
                    fileProps.load(is);
                } else {
                    log.error("Configuration file not found: {}", configPath);
                    throw new ConfigurationException("Configuration file not found: " + configPath);
                }
            }

            // Convert Properties → Map<String, String>
            Map<String, String> fileMap = fileProps.entrySet().stream()
                    .collect(Collectors.toMap(
                            e -> e.getKey().toString(),
                            e -> e.getValue().toString()));

            // Merge: CLI > file
            return ParameterTool.fromMap(fileMap).mergeWith(cliParams);
        } catch (IOException e) {
            throw new ConfigurationException("Error reading configuration from config file: ", e);
        }
    }
}
