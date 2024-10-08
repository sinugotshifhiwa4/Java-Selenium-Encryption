package com.encryption.config;

import com.encryption.SharedComponents.WebActions;
import org.apache.logging.log4j.Logger;

import javax.crypto.SecretKey;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

public class DataEncryptor {

    private static final WebActions webActions = new WebActions();
    private static final String uatPath = System.getProperty("user.dir") + "/src/main/resources/uat.properties";

    private static final Logger logger = LoggerFactory.getLogger(DataEncryptor.class);

    public static void main(String [] args){

        try{
            SecretKey secretKey = EncryptionUtil.generateKey();
            String encodeSecretKey = EncryptionUtil.encodeSecretKey(secretKey);

            webActions.writeSecretKeyToFile(encodeSecretKey);
            logger.info("Key was successfully written to file");

            //
            encryptAndSaveCredentials(secretKey);

        } catch (Exception e){
            logger.error("Error occurred while generating key: {}", e.getMessage(), e);
            throw new RuntimeException("Error occurred while generating key: " + e.getMessage(), e);
        }

    }

    private static void encryptAndSaveCredentials(SecretKey secretKey) {
        if (secretKey == null) {
            throw new IllegalArgumentException("SecretKey must not be null.");
        }

        Properties properties = loadPropertiesFromFile(); // Load properties from the uatPath

        // Encrypt individual properties
        encryptProperty(properties, "URL", secretKey);
        encryptProperty(properties, "USERNAME", secretKey);
        encryptProperty(properties, "PASSWORD", secretKey);

        // Save encrypted properties to the specified output path
        savePropertiesToFile(properties);
    }

    private static Properties loadPropertiesFromFile() {
        Properties properties = new Properties();
        try (FileInputStream inputStream = new FileInputStream(DataEncryptor.uatPath)) {
            properties.load(inputStream);
        } catch (IOException e) {
            System.err.println("Could not load properties file: " + e.getMessage());
        }
        return properties;
    }

    private static void encryptProperty(Properties properties, String key, SecretKey secretKey) {
        String value = properties.getProperty(key);
        if (value != null) {
            String encryptedValue = EncryptionUtil.encrypt(value, secretKey);
            if (encryptedValue != null) {
                properties.setProperty(key, encryptedValue);
            } else {
                System.err.println("Failed to encrypt the property: " + key);
            }
        }
    }

    private static void savePropertiesToFile(Properties properties) {
        try (FileOutputStream outputStream = new FileOutputStream(DataEncryptor.uatPath)) {
            properties.store(outputStream, null);
            System.out.println("Encrypted properties saved to " + DataEncryptor.uatPath);
        } catch (IOException e) {
            System.err.println("Could not save properties file: " + e.getMessage());
        }
    }
}
