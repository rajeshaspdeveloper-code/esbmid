package com.esb.plugin;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Standard request object passed to all plugins
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PluginRequest {

    private String requestId;
    private String branchCode;
    private String endpoint;
    private String method;
    private Map<String, Object> headers;
    private Object payload;
    private Map<String, Object> parameters;
    private LocalDateTime timestamp;
    private String sourceSystem;
    private String correlationId;

    public PluginRequest() {
        this.headers = new HashMap<>();
        this.parameters = new HashMap<>();
        this.timestamp = LocalDateTime.now();
    }

    public PluginRequest(String requestId, String branchCode, String endpoint) {
        this();
        this.requestId = requestId;
        this.branchCode = branchCode;
        this.endpoint = endpoint;
    }

    // Getters and Setters
    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
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

    public Map<String, Object> getHeaders() {
        return headers;
    }

    public void setHeaders(Map<String, Object> headers) {
        this.headers = headers != null ? headers : new HashMap<>();
    }

    public Object getPayload() {
        return payload;
    }

    public void setPayload(Object payload) {
        this.payload = payload;
    }

    public Map<String, Object> getParameters() {
        return parameters;
    }

    public void setParameters(Map<String, Object> parameters) {
        this.parameters = parameters != null ? parameters : new HashMap<>();
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public String getSourceSystem() {
        return sourceSystem;
    }

    public void setSourceSystem(String sourceSystem) {
        this.sourceSystem = sourceSystem;
    }

    public String getCorrelationId() {
        return correlationId;
    }

    public void setCorrelationId(String correlationId) {
        this.correlationId = correlationId;
    }

    // Helper methods
    public void addHeader(String key, Object value) {
        this.headers.put(key, value);
    }

    public void addParameter(String key, Object value) {
        this.parameters.put(key, value);
    }

    @Override
    public String toString() {
        return "PluginRequest{" +
                "requestId='" + requestId + '\'' +
                ", branchCode='" + branchCode + '\'' +
                ", endpoint='" + endpoint + '\'' +
                ", method='" + method + '\'' +
                ", sourceSystem='" + sourceSystem + '\'' +
                ", correlationId='" + correlationId + '\'' +
                ", timestamp=" + timestamp +
                '}';
    }
}