package com.encryption.utils;

import com.encryption.config.EncryptionUtil;
import com.encryption.config.LoggerFactory;
import org.apache.logging.log4j.Logger;

import javax.crypto.SecretKey;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class SecretKeyReader {

    private static final Logger logger = LoggerFactory.getLogger(SecretKeyReader.class);

    private static final Properties properties = new Properties();
    private static final String path = System.getProperty("user.dir") + "/src/main/resources/secret_key.properties";

    static {
        loadProperties();
    }

    /**
     * Loads the secret key properties from the specified path.
     */
    public static void loadProperties() {
        try (FileInputStream inputStream = new FileInputStream(path)) {
            properties.load(inputStream);
            logger.info("Secret key properties loaded successfully from {}", path);
        } catch (IOException e) {
            logger.error("Failed to load secret key properties from {}: {}", path, e.getMessage());
            throw new RuntimeException("Error loading secret key properties: " + e.getMessage(), e);
        }
    }

    /**
     * Retrieves the secret key for the given property key.
     *
     * @param key the property key to look up
     * @return the SecretKey associated with the key
     * @throws RuntimeException if the property is not found or an error occurs
     */
    public static SecretKey getPropertyByKey(String key) {
        try {
            String encodedKey = properties.getProperty(key);

            if (encodedKey == null || encodedKey.isEmpty()) {
                logger.error("Secret key property '{}' not found or empty", key);
                throw new IOException("Secret key property '" + key + "' not found or empty");
            }

            logger.info("Successfully retrieved property '{}'", key);

            return EncryptionUtil.decodeSecretKey(encodedKey);

        } catch (IOException e) {
            logger.error("Error retrieving secret key property '{}': {}", key, e.getMessage());
            throw new RuntimeException("Error retrieving secret key property '" + key + "': " + e.getMessage(), e);
        }
    }
}

