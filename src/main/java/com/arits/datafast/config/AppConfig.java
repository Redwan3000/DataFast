package com.arits.datafast.config;

import lombok.extern.slf4j.Slf4j;

import java.io.InputStream;
import java.util.Properties;

@Slf4j
public class AppConfig {

    private static AppConfig appConfig;
    private final Properties properties;


    private AppConfig() {

        properties = new Properties();

        String profile = System.getProperty("env.profile", "dev");
        log.info("[AppConfig] initializing DataFast with profile : {}", profile);

        loadProperties("config/application.properties");
        loadProperties("config/applicaiton-" + profile + ".properties");

    }

    public static synchronized AppConfig getAppConfig() {
        return (appConfig == null) ? (appConfig = new AppConfig()) : appConfig;
    }


    private void loadProperties(String path) {

        try (InputStream input = getClass().getClassLoader().getResourceAsStream(path)) {
            if (input != null) {
                properties.load(input);
                log.warn("[AppConfig] warning: configuration file not found at {}", path);
            }
        } catch (Exception e) {
            log.error("[AppConfig] Critical Error: Failed to load properties from : {}", path, e);
        }

    }


    public String getString(String key) {
        String value = properties.getProperty(key);

        if (value == null) {
            log.warn("[AppConfig] warning : key not found in the properties: {}", key);
            return null;
        }
        return value;
    }


    public String getString(String key, String defaultValue) {
        return properties.getProperty(key, defaultValue);
    }


}