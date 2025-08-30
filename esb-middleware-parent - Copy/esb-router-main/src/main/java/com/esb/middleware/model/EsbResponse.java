package com.esb.middleware.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * ESB response model for outgoing responses
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class EsbResponse {
    
    private String requestId;
    private String status;
    private String statusCode;
    private String message;
    private Object data;
    private Map<String, Object> headers;
    private Map<String, Object> metadata;
    private LocalDateTime timestamp;
    private long processingTime;
    private String errorCode;
    private String errorMessage;
    
    public EsbResponse() {
        this.headers = new HashMap<>();
        this.metadata = new HashMap<>();
        this.timestamp = LocalDateTime.now();
        this.status = "SUCCESS";
        this.statusCode = "200";
    }
    
    public EsbResponse(String requestId) {
        this();
        this.requestId = requestId;
    }
    
    // Static factory methods
    public static EsbResponse success(String requestId, Object data) {
        EsbResponse response = new EsbResponse(requestId);
        response.setData(data);
        response.setMessage("Request processed successfully");
        return response;
    }
    
    public static EsbResponse error(String requestId, String errorCode, String errorMessage) {
        EsbResponse response = new EsbResponse(requestId);
        response.setStatus("ERROR");
        response.setStatusCode("500");
        response.setErrorCode(errorCode);
        response.setErrorMessage(errorMessage);
        response.setMessage("Request processing failed");
        return response;
    }
    
    public static EsbResponse businessError(String requestId, String statusCode, String message) {
        EsbResponse response = new EsbResponse(requestId);
        response.setStatus("BUSINESS_ERROR");
        response.setStatusCode(statusCode);
        response.setMessage(message);
        return response;
    }
    
    public static EsbResponse notFound(String requestId, String branchCode, String endpoint) {
        EsbResponse response = new EsbResponse(requestId);
        response.setStatus("NOT_FOUND");
        response.setStatusCode("404");
        response.setMessage("No route found for branch: " + branchCode + ", endpoint: " + endpoint);
        return response;
    }
    
    // Getters and Setters
    public String getRequestId() {
        return requestId;
    }
    
    public void setRequestId(String requestId) {
        this.requestId = requestId;
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
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    public Object getData() {
        return data;
    }
    
    public void setData(Object data) {
        this.data = data;
    }
    
    public Map<String, Object> getHeaders() {
        return headers;
    }
    
    public void setHeaders(Map<String, Object> headers) {
        this.headers = headers != null ? headers : new HashMap<>();
    }
    
    public Map<String, Object> getMetadata() {
        return metadata;
    }
    
    public void setMetadata(Map<String, Object> metadata) {
        this.metadata = metadata != null ? metadata : new HashMap<>();
    }
    
    public LocalDateTime getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
    
    public long getProcessingTime() {
        return processingTime;
    }
    
    public void setProcessingTime(long processingTime) {
        this.processingTime = processingTime;
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
    
    // Helper methods
    public void addHeader(String key, Object value) {
        if (this.headers == null) {
            this.headers = new HashMap<>();
        }
        this.headers.put(key, value);
    }
    
    public void addMetadata(String key, Object value) {
        if (this.metadata == null) {
            this.metadata = new HashMap<>();
        }
        this.metadata.put(key, value);
    }
    
    public boolean isSuccess() {
        return "SUCCESS".equals(this.status);
    }
    
    public boolean isError() {
        return "ERROR".equals(this.status) || "BUSINESS_ERROR".equals(this.status);
    }
    
    @Override
    public String toString() {
        return "EsbResponse{" +
                "requestId='" + requestId + '\'' +
                ", status='" + status + '\'' +
                ", statusCode='" + statusCode + '\'' +
                ", message='" + message + '\'' +
                ", processingTime=" + processingTime +
                ", timestamp=" + timestamp +
                '}';
    }
}