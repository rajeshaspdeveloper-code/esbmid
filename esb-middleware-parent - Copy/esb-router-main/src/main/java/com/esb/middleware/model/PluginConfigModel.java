package com.esb.middleware.model;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Plugin configuration model for database storage
 */
public class PluginConfigModel {
    
    private Long id;
    private String pluginId;
    private String pluginName;
    private String version;
    private String jarFileName;
    private String mainClass;
    private String description;
    private String author;
    private String vendor;
    private String supportedEndpoints;
    private Map<String, String> configuration;
    private boolean enabled;
    private int priority;
    private String status;
    private LocalDateTime loadedTime;
    private String createdBy;
    private LocalDateTime createdDate;
    private String modifiedBy;
    private LocalDateTime modifiedDate;
    
    public PluginConfigModel() {}
    
    public PluginConfigModel(String pluginId, String jarFileName, String mainClass) {
        this.pluginId = pluginId;
        this.jarFileName = jarFileName;
        this.mainClass = mainClass;
        this.enabled = true;
        this.priority = 0;
        this.status = "LOADED";
        this.loadedTime = LocalDateTime.now();
        this.createdDate = LocalDateTime.now();
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getPluginId() {
        return pluginId;
    }
    
    public void setPluginId(String pluginId) {
        this.pluginId = pluginId;
    }
    
    public String getPluginName() {
        return pluginName;
    }
    
    public void setPluginName(String pluginName) {
        this.pluginName = pluginName;
    }
    
    public String getVersion() {
        return version;
    }
    
    public void setVersion(String version) {
        this.version = version;
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
    
    public String getSupportedEndpoints() {
        return supportedEndpoints;
    }
    
    public void setSupportedEndpoints(String supportedEndpoints) {
        this.supportedEndpoints = supportedEndpoints;
    }
    
    public Map<String, String> getConfiguration() {
        return configuration;
    }
    
    public void setConfiguration(Map<String, String> configuration) {
        this.configuration = configuration;
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
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public LocalDateTime getLoadedTime() {
        return loadedTime;
    }
    
    public void setLoadedTime(LocalDateTime loadedTime) {
        this.loadedTime = loadedTime;
    }
    
    public String getCreatedBy() {
        return createdBy;
    }
    
    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }
    
    public LocalDateTime getCreatedDate() {
        return createdDate;
    }
    
    public void setCreatedDate(LocalDateTime createdDate) {
        this.createdDate = createdDate;
    }
    
    public String getModifiedBy() {
        return modifiedBy;
    }
    
    public void setModifiedBy(String modifiedBy) {
        this.modifiedBy = modifiedBy;
    }
    
    public LocalDateTime getModifiedDate() {
        return modifiedDate;
    }
    
    public void setModifiedDate(LocalDateTime modifiedDate) {
        this.modifiedDate = modifiedDate;
    }
    
    @Override
    public String toString() {
        return "PluginConfigModel{" +
                "id=" + id +
                ", pluginId='" + pluginId + '\'' +
                ", pluginName='" + pluginName + '\'' +
                ", version='" + version + '\'' +
                ", jarFileName='" + jarFileName + '\'' +
                ", enabled=" + enabled +
                ", status='" + status + '\'' +
                '}';
    }
}