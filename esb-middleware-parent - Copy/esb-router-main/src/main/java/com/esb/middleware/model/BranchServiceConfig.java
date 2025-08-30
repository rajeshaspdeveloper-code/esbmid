package com.esb.middleware.model;

import java.time.LocalDateTime;
import java.util.Map;

public class BranchServiceConfig {
    private Long id;
    private String branchCode;
    private String serviceId;
    private String pluginId;
    private String targetUrl;
    private String serviceEndpoint;
    private String esbService;
    private boolean enabled;
    private int timeoutMs;
    private int retryCount;
    private Map<String, String> headers;
    private Map<String, String> parameters;
    private LocalDateTime createdDate;
    private LocalDateTime modifiedDate;
    private String createdBy;
    private String modifiedBy;
    
    // Constructors
    public BranchServiceConfig() {}
    
    public BranchServiceConfig(String branchCode, String serviceId, String pluginId) {
        this.branchCode = branchCode;
        this.serviceId = serviceId;
        this.pluginId = pluginId;
        this.enabled = true;
        this.timeoutMs = 30000;
        this.retryCount = 3;
        this.createdDate = LocalDateTime.now();
    }
    
    // Getters and setters (all standard getters/setters)
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getBranchCode() { return branchCode; }
    public void setBranchCode(String branchCode) { this.branchCode = branchCode; }
    
    public String getServiceId() { return serviceId; }
    public void setServiceId(String serviceId) { this.serviceId = serviceId; }
    
    public String getPluginId() { return pluginId; }
    public void setPluginId(String pluginId) { this.pluginId = pluginId; }
    
    public String getTargetUrl() { return targetUrl; }
    public void setTargetUrl(String targetUrl) { this.targetUrl = targetUrl; }
    
    public String getServiceEndpoint() { return serviceEndpoint; }
    public void setServiceEndpoint(String serviceEndpoint) { this.serviceEndpoint = serviceEndpoint; }
    
    public String getEsbService() { return esbService; }
    public void setEsbService(String esbService) { this.esbService = esbService; }
    
    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
    
    public int getTimeoutMs() { return timeoutMs; }
    public void setTimeoutMs(int timeoutMs) { this.timeoutMs = timeoutMs; }
    
    public int getRetryCount() { return retryCount; }
    public void setRetryCount(int retryCount) { this.retryCount = retryCount; }
    
    public Map<String, String> getHeaders() { return headers; }
    public void setHeaders(Map<String, String> headers) { this.headers = headers; }
    
    public Map<String, String> getParameters() { return parameters; }
    public void setParameters(Map<String, String> parameters) { this.parameters = parameters; }
    
    public LocalDateTime getCreatedDate() { return createdDate; }
    public void setCreatedDate(LocalDateTime createdDate) { this.createdDate = createdDate; }
    
    public LocalDateTime getModifiedDate() { return modifiedDate; }
    public void setModifiedDate(LocalDateTime modifiedDate) { this.modifiedDate = modifiedDate; }
    
    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }
    
    public String getModifiedBy() { return modifiedBy; }
    public void setModifiedBy(String modifiedBy) { this.modifiedBy = modifiedBy; }
    
    @Override
    public String toString() {
        return "BranchServiceConfig{" +
                "id=" + id +
                ", branchCode='" + branchCode + '\'' +
                ", serviceId='" + serviceId + '\'' +
                ", pluginId='" + pluginId + '\'' +
                ", targetUrl='" + targetUrl + '\'' +
                ", enabled=" + enabled +
                '}';
    }
}