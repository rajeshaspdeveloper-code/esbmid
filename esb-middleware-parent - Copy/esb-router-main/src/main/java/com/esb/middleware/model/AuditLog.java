package com.esb.middleware.model;

import java.time.LocalDateTime;

/**
 * Audit log model for tracking all ESB transactions
 */
public class AuditLog {
    
    private Long id;
    private String requestId;
    private String correlationId;
    private String branchCode;
    private String endpoint;
    private String method;
    private String pluginId;
    private String sourceSystem;
    private String sourceIp;
    private String requestPayload;
    private String responsePayload;
    private String status;
    private String statusCode;
    private String errorCode;
    private String errorMessage;
    private long processingTime;
    private LocalDateTime requestTime;
    private LocalDateTime responseTime;
    private String headers;
    private String parameters;
    private String additionalInfo;
    
    public AuditLog() {}
    
    public AuditLog(String requestId, String branchCode, String endpoint) {
        this.requestId = requestId;
        this.branchCode = branchCode;
        this.endpoint = endpoint;
        this.requestTime = LocalDateTime.now();
        this.status = "PROCESSING";
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getRequestId() {
        return requestId;
    }
    
    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }
    
    public String getCorrelationId() {
        return correlationId;
    }
    
    public void setCorrelationId(String correlationId) {
        this.correlationId = correlationId;
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
    
    public String getSourceSystem() {
        return sourceSystem;
    }
    
    public void setSourceSystem(String sourceSystem) {
        this.sourceSystem = sourceSystem;
    }
    
    public String getSourceIp() {
        return sourceIp;
    }
    
    public void setSourceIp(String sourceIp) {
        this.sourceIp = sourceIp;
    }
    
    public String getRequestPayload() {
        return requestPayload;
    }
    
    public void setRequestPayload(String requestPayload) {
        this.requestPayload = requestPayload;
    }
    
    public String getResponsePayload() {
        return responsePayload;
    }
    
    public void setResponsePayload(String responsePayload) {
        this.responsePayload = responsePayload;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public String getStatusCode() {
        return statusCode;
    }
    
    public void setStatusCode(String statusCode) {
        this.statusCode = statusCode;
    }
    
    public String getErrorCode() {
        return errorCode;
    }
    
    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }
    
    public String getErrorMessage() {
        return errorMessage;
    }
    
    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
    
    public long getProcessingTime() {
        return processingTime;
    }
    
    public void setProcessingTime(long processingTime) {
        this.processingTime = processingTime;
    }
    
    public LocalDateTime getRequestTime() {
        return requestTime;
    }
    
    public void setRequestTime(LocalDateTime requestTime) {
        this.requestTime = requestTime;
    }
    
    public LocalDateTime getResponseTime() {
        return responseTime;
    }
    
    public void setResponseTime(LocalDateTime responseTime) {
        this.responseTime = responseTime;
    }
    
    public String getHeaders() {
        return headers;
    }
    
    public void setHeaders(String headers) {
        this.headers = headers;
    }
    
    public String getParameters() {
        return parameters;
    }
    
    public void setParameters(String parameters) {
        this.parameters = parameters;
    }
    
    public String getAdditionalInfo() {
        return additionalInfo;
    }
    
    public void setAdditionalInfo(String additionalInfo) {
        this.additionalInfo = additionalInfo;
    }
    
    @Override
    public String toString() {
        return "AuditLog{" +
                "id=" + id +
                ", requestId='" + requestId + '\'' +
                ", branchCode='" + branchCode + '\'' +
                ", endpoint='" + endpoint + '\'' +
                ", pluginId='" + pluginId + '\'' +
                ", status='" + status + '\'' +
                ", processingTime=" + processingTime +
                ", requestTime=" + requestTime +
                '}';
    }
}