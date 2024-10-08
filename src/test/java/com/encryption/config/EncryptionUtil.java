package com.encryption.config;

import org.apache.logging.log4j.Logger;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.Base64;

public class EncryptionUtil {

    private static final Logger logger = LoggerFactory.getLogger(EncryptionUtil.class);

    private static final String ALGORITHM = "AES";
    private static final String CIPHER_TRANSFORMATION = "AES/CBC/PKCS5Padding";
    private static final int KEY_SIZE = 256;
    private static final int IV_SIZE = 16;

    public static SecretKey generateKey() {
        try {
            KeyGenerator keyGen = KeyGenerator.getInstance(ALGORITHM);
            keyGen.init(KEY_SIZE);
            return keyGen.generateKey();
        } catch (NoSuchAlgorithmException e) {
            logger.error("Error while generating secret key: {}", e.getMessage(), e);;
            throw new RuntimeException("Error while generating secret key: " + e.getMessage(), e);
        }
    }

    public static SecretKey generateKeyFromPassword(char[] password, byte[] salt) {
        try {
            SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
            KeySpec spec = new PBEKeySpec(password, salt, 65536, KEY_SIZE); // 65536 iterations, 256-bit key
            SecretKey tmp = new SecretKeySpec(factory.generateSecret(spec).getEncoded(), ALGORITHM);
            return tmp;
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            logger.error("Error while generating key from password: {}", e.getMessage(), e);
            throw new RuntimeException("Error while generating key from password: " + e.getMessage(), e);
        }
    }

    public static String encrypt(String strToEncrypt, SecretKey secretKey) {
        try {
            Cipher cipher = Cipher.getInstance(CIPHER_TRANSFORMATION);
            byte[] iv = new byte[IV_SIZE];
            SecureRandom random = SecureRandom.getInstanceStrong(); // Stronger random IV generation
            random.nextBytes(iv); // Generate a random IV
            IvParameterSpec ivParams = new IvParameterSpec(iv);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, ivParams);

            byte[] encryptedBytes = cipher.doFinal(strToEncrypt.getBytes(StandardCharsets.UTF_8));
            // Combine IV and ciphertext
            byte[] combined = new byte[IV_SIZE + encryptedBytes.length];
            System.arraycopy(iv, 0, combined, 0, IV_SIZE);
            System.arraycopy(encryptedBytes, 0, combined, IV_SIZE, encryptedBytes.length);

            return Base64.getEncoder().encodeToString(combined); // Encode to Base64
        } catch (Exception e) {
            logger.error("Error while encrypting: {}", e.getMessage(), e);
            throw new RuntimeException("Error while encrypting: " + e.getMessage(), e);
        }
    }

    public static String decrypt(String strToDecrypt, SecretKey secretKey) {
        try {
            byte[] combined = Base64.getDecoder().decode(strToDecrypt);

            if (combined.length < IV_SIZE) {
                throw new IllegalArgumentException("Invalid encrypted data length");
            }

            byte[] iv = new byte[IV_SIZE];
            System.arraycopy(combined, 0, iv, 0, IV_SIZE); // Extract IV
            byte[] encryptedBytes = new byte[combined.length - IV_SIZE];
            System.arraycopy(combined, IV_SIZE, encryptedBytes, 0, encryptedBytes.length);

            Cipher cipher = Cipher.getInstance(CIPHER_TRANSFORMATION);
            IvParameterSpec ivParams = new IvParameterSpec(iv);
            cipher.init(Cipher.DECRYPT_MODE, secretKey, ivParams);

            byte[] decryptedBytes = cipher.doFinal(encryptedBytes);
            return new String(decryptedBytes, StandardCharsets.UTF_8);
        } catch (Exception e) {
            logger.error("Error while decrypting: {}", e.getMessage(), e);
            throw new RuntimeException("Error while decrypting: " + e.getMessage(), e);
        }
    }

    public static SecretKey decodeSecretKey(String encodedKey) {
        try {
            byte[] decodedKey = Base64.getDecoder().decode(encodedKey);
            return new SecretKeySpec(decodedKey, ALGORITHM);
        } catch (IllegalArgumentException e) {
            logger.error("Error while decoding secret key: {}", e.getMessage(), e);
            throw new RuntimeException("Error while decoding secret key: " + e.getMessage(), e);
        }
    }

    public static String encodeSecretKey(SecretKey secretKey) {
        try {
            return Base64.getEncoder().encodeToString(secretKey.getEncoded());
        } catch (Exception e) {
            logger.error("Error while encoding secret key: {}", e.getMessage(), e);
            throw new RuntimeException("Error while encoding secret key: " + e.getMessage(), e);
        }
    }
}

