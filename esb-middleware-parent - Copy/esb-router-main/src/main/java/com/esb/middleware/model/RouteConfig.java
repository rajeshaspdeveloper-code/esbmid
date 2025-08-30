package com.esb.middleware.model;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Route configuration model
 */
public class RouteConfig {
    
    private Long id;
    private String branchCode;
    private String endpoint;
    private String method;
    private String pluginId;
    private String targetUrl;
    private Map<String, String> headers;
    private Map<String, String> parameters;
    private String transformationRule;
    private boolean enabled;
    private int priority;
    private int timeout;
    private int retryCount;
    private String createdBy;
    private LocalDateTime createdDate;
    private String modifiedBy;
    private LocalDateTime modifiedDate;
    private String description;
    
    public RouteConfig() {}
    
    public RouteConfig(String branchCode, String endpoint, String pluginId) {
        this.branchCode = branchCode;
        this.endpoint = endpoint;
        this.pluginId = pluginId;
        this.enabled = true;
        this.priority = 0;
        this.timeout = 30000;
        this.retryCount = 3;
        this.createdDate = LocalDateTime.now();
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getBranchCode() {
        return branchCode;
    }
    
    public void setBranchCode(String branchCode) {
        this.branchCode = branchCode;
    }
    
    public String getEndpoint() {
        return endpoint;
    }
    
    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }
    
    public String getMethod() {
        return method;
    }
    
    public void setMethod(String method) {
        this.method = method;
    }
    
    public String getPluginId() {
        return pluginId;
    }
    
    public void setPluginId(String pluginId) {
        this.pluginId = pluginId;
    }
    
    public String getTargetUrl() {
        return targetUrl;
    }
    
    public void setTargetUrl(String targetUrl) {
        this.targetUrl = targetUrl;
    }
    
    public Map<String, String> getHeaders() {
        return headers;
    }
    
    public void setHeaders(Map<String, String> headers) {
        this.headers = headers;
    }
    
    public Map<String, String> getParameters() {
        return parameters;
    }
    
    public void setParameters(Map<String, String> parameters) {
        this.parameters = parameters;
    }
    
    public String getTransformationRule() {
        return transformationRule;
    }
    
    public void setTransformationRule(String transformationRule) {
        this.transformationRule = transformationRule;
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
    
    public int getTimeout() {
        return timeout;
    }
    
    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }
    
    public int getRetryCount() {
        return retryCount;
    }
    
    public void setRetryCount(int retryCount) {
        this.retryCount = retryCount;
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
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    @Override
    public String toString() {
        return "RouteConfig{" +
                "id=" + id +
                ", branchCode='" + branchCode + '\'' +
                ", endpoint='" + endpoint + '\'' +
                ", method='" + method + '\'' +
                ", pluginId='" + pluginId + '\'' +
                ", enabled=" + enabled +
                ", priority=" + priority +
                '}';
    }
}