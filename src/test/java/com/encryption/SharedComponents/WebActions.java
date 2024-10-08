package com.encryption.SharedComponents;

import com.encryption.config.LoggerFactory;
import org.apache.logging.log4j.Logger;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Properties;

public class WebActions {

    private static final Logger logger = LoggerFactory.getLogger(WebActions.class);

    public void writeSecretKeyToFile(String secretKey) {
        if (secretKey == null || secretKey.isEmpty()) {
            logger.error("Attempted to write a null or empty secret key.");
            throw new IllegalArgumentException("Secret key cannot be null or empty.");
        }

        Properties properties = new Properties();
        properties.setProperty("SECRET_KEY", secretKey);
        String path = System.getProperty("user.dir") + "/src/main/resources/secret_key.properties";

        try (BufferedWriter writer = Files.newBufferedWriter(Paths.get(path))) {
            properties.store(writer, null); // Store properties to file
            logger.info("Secret key successfully written to file at {}", path);
        } catch (IOException e) {
            logger.fatal("Error while writing secret key to file at {}: {}", path, e.getMessage(), e);
            throw new RuntimeException("Error while writing to file at " + path, e);
        }
    }

}
