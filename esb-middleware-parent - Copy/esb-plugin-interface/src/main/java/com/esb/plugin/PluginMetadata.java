package com.esb.plugin;

import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;

/**
 * Plugin metadata information
 */
public class PluginMetadata {

    private String pluginId;
    private String n;
    private String version;
    private String description;
    private String author;
    private String vendor;
    private List<String> supportedEndpoints;
    private Map<String, String> configuration;
    private String jarFileName;
    private String mainClass;
    private boolean enabled;
    private int priority;
    private long loadedTime;

    public PluginMetadata() {
        this.supportedEndpoints = new ArrayList<>();
        this.configuration = new HashMap<>();
        this.enabled = true;
        this.priority = 0;
        this.loadedTime = System.currentTimeMillis();
    }

    public PluginMetadata(String pluginId, String n, String version) {
        this();
        this.pluginId = pluginId;
        this.n = n;
        this.version = version;
    }

    // Getters and Setters
    public String getPluginId() {
        return pluginId;
    }

    public void setPluginId(String pluginId) {
        this.pluginId = pluginId;
    }

    public String getName() {
        return n;
    }

    public void setName(String n) {
        this.n = n;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getVendor() {
        return vendor;
    }

    public void setVendor(String vendor) {
        this.vendor = vendor;
    }

    public List<String> getSupportedEndpoints() {
        return supportedEndpoints;
    }

    public void setSupportedEndpoints(List<String> supportedEndpoints) {
        this.supportedEndpoints = supportedEndpoints != null ? supportedEndpoints : new ArrayList<>();
    }

    public Map<String, String> getConfiguration() {
        return configuration;
    }

    public void setConfiguration(Map<String, String> configuration) {
        this.configuration = configuration != null ? configuration : new HashMap<>();
    }

    public String getJarFileName() {
        return jarFileName;
    }

    public void setJarFileName(String jarFileName) {
        this.jarFileName = jarFileName;
    }

    public String getMainClass() {
        return mainClass;
    }

    public void setMainClass(String mainClass) {
        this.mainClass = mainClass;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public long getLoadedTime() {
        return loadedTime;
    }

    public void setLoadedTime(long loadedTime) {
        this.loadedTime = loadedTime;
    }

    // Helper methods
    public void addSupportedEndpoint(String endpoint) {
        if (this.supportedEndpoints == null) {
            this.supportedEndpoints = new ArrayList<>();
        }
        this.supportedEndpoints.add(endpoint);
    }

    public void addConfiguration(String key, String value) {
        if (this.configuration == null) {
            this.configuration = new HashMap<>();
        }
        this.configuration.put(key, value);
    }

    @Override
    public String toString() {
        return "PluginMetadata{" +
                "pluginId='" + pluginId + '\'' +
                ", n='" + n + '\'' +
                ", version='" + version + '\'' +
                ", author='" + author + '\'' +
                ", enabled=" + enabled +
                ", priority=" + priority +
                ", supportedEndpoints=" + supportedEndpoints +
                '}';
    }
}