package com.encryption.utils;

import com.encryption.config.LoggerFactory;
import org.apache.logging.log4j.Logger;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class ConfigUat {

    private static final Logger logger = LoggerFactory.getLogger(ConfigUat.class);

    private static final Properties properties = new Properties();
    private static final String path = System.getProperty("user.dir") + "/src/main/resources/uat.properties";

    static {
        loadProperties();
    }

    /**
     * Loads the UAT configuration properties from the specified path.
     */
    public static void loadProperties() {
        try (FileInputStream inputStream = new FileInputStream(path)) {
            properties.load(inputStream);
            logger.info("UAT properties loaded successfully from {}", path);
        } catch (IOException e) {
            logger.error("Failed to load UAT properties from {}: {}", path, e.getMessage());
            throw new RuntimeException("Error loading UAT properties: " + e.getMessage());
        }
    }

    /**
     * Retrieves the property value for the given key.
     *
     * @param key the property key to look up
     * @return the value associated with the key
     * @throws RuntimeException if the property is not found or an error occurs
     */
    public static String getPropertyByKey(String key) {
        try {
            String value = properties.getProperty(key);

            if (value == null || value.isEmpty()) {
                logger.error("Property '{}' not found or empty in UAT properties", key);
                throw new IOException("Property '" + key + "' not found or empty");
            }

            logger.info("Successfully retrieved property '{}'", key);
            return value;

        } catch (IOException e) {
            logger.error("Error retrieving property '{}': {}", key, e.getMessage());
            throw new RuntimeException("Error retrieving property '" + key + "': " + e.getMessage());
        }
    }
}

