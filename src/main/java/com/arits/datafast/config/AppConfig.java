package com.arits.datafast.config;

import java.io.InputStream;
import java.util.Properties;

public class AppConfig {

    // The single instance of this class
    private static AppConfig instance;
    private final Properties properties;

    private AppConfig() {
        properties = new Properties();

        // 1. Determine the active profile. It defaults to "dev" if you don't specify one.
        String profile = System.getProperty("env.profile", "dev");
        System.out.println("[AppConfig] Initializing DataFast with profile: " + profile);

        // 2. Load the base shared properties first (if you have standard ones)
        loadProperties("config/application.properties");

        // 3. Load the environment-specific properties (this overwrites base properties if there are duplicates)
        loadProperties("config/application-" + profile + ".properties");
    }

    // Synchronized ensures thread-safety if multiple background tasks try to access it at once
    public static synchronized AppConfig getInstance() {
        if (instance == null) {
            instance = new AppConfig();
        }
        return instance;
    }

    private void loadProperties(String path) {
        // We use the classloader because these files live inside the compiled resources folder
        try (InputStream input = getClass().getClassLoader().getResourceAsStream(path)) {
            if (input != null) {
                properties.load(input);
            } else {
                System.err.println("[AppConfig] Warning: Configuration file not found at " + path);
            }
        } catch (Exception e) {
            System.err.println("[AppConfig] Critical Error: Failed to load properties from " + path);
            e.printStackTrace();
        }
    }

    // --- Helper Methods to grab your keys ---

    public String getString(String key) {
        String value = properties.getProperty(key);
        if (value == null) {
            System.err.println("[AppConfig] Missing key requested: " + key);
        }
        return value;
    }

    public String getString(String key, String defaultValue) {
        return properties.getProperty(key, defaultValue);
    }

    public int getInt(String key) {
        String val = properties.getProperty(key);
        return val != null ? Integer.parseInt(val.trim()) : 0;
    }

    public boolean getBoolean(String key) {
        String val = properties.getProperty(key);
        return val != null && Boolean.parseBoolean(val.trim());
    }
}