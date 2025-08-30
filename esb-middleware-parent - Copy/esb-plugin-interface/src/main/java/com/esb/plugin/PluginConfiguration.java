package com.esb.plugin;

import java.util.Map;
import java.util.HashMap;
import java.util.Properties;

/**
 * Plugin configuration container
 */
public class PluginConfiguration {
    
    private String pluginId;
    private Map<String, String> properties;
    private Map<String, Object> settings;
    private String environment;
    private boolean debugMode;
    
    public PluginConfiguration() {
        this.properties = new HashMap<>();
        this.settings = new HashMap<>();
        this.environment = "Dev";
        this.debugMode = true;
    }
    
    public PluginConfiguration(String pluginId) {
        this();
        this.pluginId = pluginId;
    }
    
    // Getters and Setters
    public String getPluginId() {
        return pluginId;
    }
    
    public void setPluginId(String pluginId) {
        this.pluginId = pluginId;
    }
    
    public Map<String, String> getProperties() {
        return properties;
    }
    
    public void setProperties(Map<String, String> properties) {
        this.properties = properties != null ? properties : new HashMap<>();
    }
    
    public Map<String, Object> getSettings() {
        return settings;
    }
    
    public void setSettings(Map<String, Object> settings) {
        this.settings = settings != null ? settings : new HashMap<>();
    }
    
    public String getEnvironment() {
        return environment;
    }
    
    public void setEnvironment(String environment) {
        this.environment = environment;
    }
    
    public boolean isDebugMode() {
        return debugMode;
    }
    
    public void setDebugMode(boolean debugMode) {
        this.debugMode = debugMode;
    }
    
    // Helper methods
    public String getProperty(String key) {
        return properties.get(key);
    }
    
    public String getProperty(String key, String defaultValue) {
        return properties.getOrDefault(key, defaultValue);
    }
    
    public void setProperty(String key, String value) {
        this.properties.put(key, value);
    }
    
    public Object getSetting(String key) {
        return settings.get(key);
    }
    
    public <T> T getSetting(String key, Class<T> type) {
        Object value = settings.get(key);
        if (value != null && type.isAssignableFrom(value.getClass())) {
            return type.cast(value);
        }
        return null;
    }
    
    public void setSetting(String key, Object value) {
        this.settings.put(key, value);
    }
    
    public Properties asProperties() {
        Properties props = new Properties();
        props.putAll(this.properties);
        return props;
    }
    
    @Override
    public String toString() {
        return "PluginConfiguration{" +
                "pluginId='" + pluginId + '\'' +
                ", environment='" + environment + '\'' +
                ", debugMode=" + debugMode +
                ", properties=" + properties.size() + " items" +
                ", settings=" + settings.size() + " items" +
                '}';
    }
}