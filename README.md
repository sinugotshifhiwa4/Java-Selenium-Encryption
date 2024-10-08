

---

# Encryption and Decryption in Java with Logging

## Overview

This project demonstrates a secure method for encryption and decryption using **AES** (Advanced Encryption Standard) along with a robust logging system powered by **Log4j2**. The project incorporates **Selenium WebDriver** for browser-based interactions and automating tasks.

The project structure:
- **src/main/java**: Contains core encryption utilities, configuration, and WebDriver actions.
- **src/main/resources**: Stores properties files for environment variables and secret key storage.
- **src/test/java**: Contains test cases to validate encryption, decryption, and web actions.
- **logs/**: Stores rolling logs for both info and error levels.

---

## Project Structure

```plaintext
EncryptionAndDecryption
├── logs/
│   ├── error/
│   │   └── test_error.log
│   ├── info/
│   │   └── test_run.log
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/encryption/
│   │   │       ├── config/
│   │   │       │   ├── DataEncryptor.java
│   │   │       │   ├── EncryptionUtil.java
│   │   │       │   ├── LoggerFactory.java
│   │   │       └── utils/
│   │   │           ├── ConfigUat.java
│   │   │           └── SecretKeyReader.java
│   │   └── resources/
│   │       ├── secret_key.properties
│   │       └── uat.properties
│   └── test/
│       └── java/
│           └── com/encryption/tests/
│               └── DemoTest.java
├── pom.xml
└── log4j2.xml
```

---

## Dependencies

- **Maven**: Dependency management.
- **Selenium WebDriver**: Automating browser tasks.
- **Log4j2**: Logging system.
- **Java Cryptography Architecture (JCA)**: Used for secure encryption and decryption.

Add the following dependencies in `pom.xml`:

```xml
<dependencies>
    <!-- Logging -->
    <dependency>
        <groupId>org.apache.logging.log4j</groupId>
        <artifactId>log4j-core</artifactId>
        <version>2.x.x</version>
    </dependency>
    <dependency>
        <groupId>org.apache.logging.log4j</groupId>
        <artifactId>log4j-api</artifactId>
        <version>2.x.x</version>
    </dependency>

    <!-- Selenium -->
    <dependency>
        <groupId>org.seleniumhq.selenium</groupId>
        <artifactId>selenium-java</artifactId>
        <version>4.x.x</version>
    </dependency>
</dependencies>
```

---

## Encryption and Decryption

The project utilizes **AES-256** encryption in **CBC mode** with **PKCS5Padding**. The methods for generating secret keys, encrypting, and decrypting strings are defined in `EncryptionUtil.java`.

### Key Generation

The AES secret key can either be randomly generated or derived from a password and salt using **PBKDF2WithHmacSHA256**.

```java
SecretKey secretKey = EncryptionUtil.generateKey();  // Random key
```

To generate a key from a password:

```java
SecretKey secretKeyFromPassword = EncryptionUtil.generateKeyFromPassword(passwordChars, saltBytes);
```

### Encryption Example

```java
String encryptedValue = EncryptionUtil.encrypt("SensitiveData", secretKey);
```

### Decryption Example

```java
String decryptedValue = EncryptionUtil.decrypt(encryptedValue, secretKey);
```

### Storing and Reading Keys

The secret key can be encoded and stored in a properties file:

```java
String encodedKey = EncryptionUtil.encodeSecretKey(secretKey);
webActions.writeSecretKeyToFile(encodedKey);
```

To read and decode the key:

```java
SecretKey secretKey = SecretKeyReader.getPropertyByKey("SECRET_KEY");
```

---

## Logging Configuration

Log4j2 is used to handle both **console** and **file-based logging**. The loggers are defined in `log4j2.xml` and controlled by a custom `LoggerFactory` class.

### log4j2.xml

```xml
<Configuration status="WARN">
    <Appenders>
        <!-- Console Appender -->
        <Console name="ConsoleAppender" target="SYSTEM_OUT">
            <PatternLayout pattern="%d{yyyy-MM-dd HH:mm:ss.SSS} [%t] %-5level %c{1} - %msg%n%throwable"/>
            <Filters>
                <ThresholdFilter level="info" onMatch="ACCEPT" onMismatch="DENY"/>
            </Filters>
        </Console>

        <!-- Rolling File Appenders for Info and Error logs -->
        <RollingFile name="InfoRollingFileAppender" fileName="logs/info/test_run.log"
                     filePattern="logs/info/test_run-%d{yyyy-MM-dd}-%i.log.gz">
            <PatternLayout pattern="%d{yyyy-MM-dd HH:mm:ss.SSS} [%t] %-5level %c{1} - %msg%n%throwable"/>
            <Policies>
                <SizeBasedTriggeringPolicy size="5MB"/>
                <TimeBasedTriggeringPolicy interval="1" modulate="true"/>
            </Policies>
        </RollingFile>

        <RollingFile name="ErrorRollingFileAppender" fileName="logs/error/test_error.log"
                     filePattern="logs/error/test_error-%d{yyyy-MM-dd}-%i.log.gz">
            <PatternLayout pattern="%d{yyyy-MM-dd HH:mm:ss.SSS} [%t] %-5level %c{1} - %msg%n%throwable"/>
            <Policies>
                <SizeBasedTriggeringPolicy size="5MB"/>
                <TimeBasedTriggeringPolicy interval="1" modulate="true"/>
            </Policies>
        </RollingFile>
    </Appenders>

    <Loggers>
        <Root level="info">
            <AppenderRef ref="ConsoleAppender"/>
            <AppenderRef ref="InfoRollingFileAppender"/>
            <AppenderRef ref="ErrorRollingFileAppender"/>
        </Root>
    </Loggers>
</Configuration>
```

### LoggerFactory

The `LoggerFactory` class handles thread-safe logger management. It uses a `ThreadLocal` to store loggers with expiration and cleanup.

```java
public static Logger getLogger(Class<?> clazz) {
    return map.computeIfAbsent(clazz, c -> new LoggerWrapper(LogManager.getLogger(c))).getLogger();
}
```

---

## Security Aspects

- **AES-256 Encryption**: Provides strong encryption by generating secure random keys and using CBC mode with a random initialization vector (IV) for each encryption operation.
- **PBKDF2 Key Derivation**: Used for password-based key derivation, ensuring that keys derived from passwords are sufficiently secure by adding a salt and iterating the function 65,536 times.
- **IV Randomization**: Ensures that even if the same data is encrypted multiple times, the ciphertext will differ, preventing attacks such as replay attacks.
- **Logging Privacy**: Sensitive data such as credentials are encrypted before logging. No sensitive information is logged in plain text.

---

## Usage

1. **Encryption**: Use `DataEncryptor` to encrypt properties (e.g., URL, USERNAME, PASSWORD).
2. **Decryption and Login**: Use `SecretKeyReader` and `EncryptionUtil` to decrypt values during login.

```java
driver.get(decrypt(ConfigUat.getPropertyByKey("URL"), secretKey));
```

---

This project showcases a secure way to handle encryption and sensitive data storage in Java, making it highly relevant for applications dealing with confidential information.

