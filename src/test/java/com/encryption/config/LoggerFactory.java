package com.encryption.config;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public final class LoggerFactory {

    // ThreadLocal to store a map of loggers per thread along with their creation timestamp
    private static final ThreadLocal<Map<Class<?>, LoggerWrapper>> loggerMap = ThreadLocal.withInitial(HashMap::new);
    private static final long EXPIRATION_TIME_MS = TimeUnit.DAYS.toMillis(14); // 14 days
    private static final int MAX_LOGGERS = 400; // Maximum number of loggers per thread

    // Private constructor to prevent instantiation
    private LoggerFactory() {}

    /**
     * Retrieves a logger for the specified class. If the logger does not
     * already exist, it will be created and stored for future use.
     *
     * @param clazz the class for which to get the logger
     * @return the Logger instance for the specified class
     */
    public static Logger getLogger(Class<?> clazz) {
        Map<Class<?>, LoggerWrapper> map = loggerMap.get(); // Get the current thread's logger map

        // Clean up expired loggers and check size limit
        cleanUpLoggers(map);

        // Create and store logger if it doesn't exist
        return map.computeIfAbsent(clazz, c -> new LoggerWrapper(LogManager.getLogger(c))).getLogger();
    }

    /**
     * Clears the logger map for the current thread, releasing any
     * resources associated with the loggers.
     */
    public static void clearLogger() {
        loggerMap.remove(); // Clear the ThreadLocal storage
    }

    /**
     * Removes the logger for the specified class from the current thread's logger map.
     *
     * @param clazz the class whose logger should be removed
     */
    public static void removeLogger(Class<?> clazz) {
        Map<Class<?>, LoggerWrapper> map = loggerMap.get();
        map.remove(clazz); // Remove the logger for the specific class
    }

    /**
     * Cleans up expired loggers and enforces the maximum logger limit.
     *
     * @param map the current thread's logger map
     */
    private static void cleanUpLoggers(Map<Class<?>, LoggerWrapper> map) {
        long currentTime = System.currentTimeMillis();

        // Remove loggers that have expired
        Iterator<Map.Entry<Class<?>, LoggerWrapper>> iterator = map.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<Class<?>, LoggerWrapper> entry = iterator.next();
            if (currentTime - entry.getValue().getCreationTime() > EXPIRATION_TIME_MS) {
                iterator.remove(); // Remove expired logger
            }
        }

        // Enforce maximum logger count
        if (map.size() > MAX_LOGGERS) {
            // Optionally remove the oldest loggers if max size is exceeded
            iterator = map.entrySet().iterator();
            while (iterator.hasNext() && map.size() > MAX_LOGGERS) {
                iterator.next();
                iterator.remove(); // Remove the oldest logger
            }
        }
    }

    /**
     * Wrapper class for Logger to hold its creation time.
     */
    private static class LoggerWrapper {
        private final Logger logger;
        private final long creationTime;

        public LoggerWrapper(Logger logger) {
            this.logger = logger;
            this.creationTime = System.currentTimeMillis(); // Capture creation time
        }

        public Logger getLogger() {
            return logger;
        }

        public long getCreationTime() {
            return creationTime;
        }
    }
}
