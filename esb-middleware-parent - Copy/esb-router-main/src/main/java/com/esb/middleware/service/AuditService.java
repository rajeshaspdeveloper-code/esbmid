package com.esb.middleware.service;

import com.esb.middleware.mapper.AuditLogMapper;
import com.esb.middleware.model.AuditLog;
import com.esb.middleware.model.EsbRequest;
import com.esb.middleware.model.EsbResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Service for managing audit logging of ESB transactions
 */
@Service
public class AuditService {
    
    private static final Logger logger = LoggerFactory.getLogger(AuditService.class);
    
    @Autowired
    private AuditLogMapper auditLogMapper;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    /**
     * Log request initiation
     */
    @Async
    public void logRequest(String requestId, EsbRequest request, String pluginId, String sourceIp) {
        try {
            AuditLog auditLog = new AuditLog();
            auditLog.setRequestId(requestId);
            auditLog.setCorrelationId(request.getCorrelationId());
            auditLog.setBranchCode(request.getBranchCode());
            auditLog.setEndpoint(request.getEndpoint());
            auditLog.setMethod(request.getMethod());
            auditLog.setPluginId(pluginId);
            auditLog.setSourceSystem(request.getSourceSystem());
            auditLog.setSourceIp(sourceIp);
            auditLog.setRequestTime(LocalDateTime.now());
            auditLog.setStatus("PROCESSING");
            
            // Serialize request payload
            if (request.getPayload() != null) {
                auditLog.setRequestPayload(serializeObject(request.getPayload()));
            }
            
            // Serialize headers and parameters
            if (request.getHeaders() != null && !request.getHeaders().isEmpty()) {
                auditLog.setHeaders(serializeObject(request.getHeaders()));
            }
            
            if (request.getParameters() != null && !request.getParameters().isEmpty()) {
                auditLog.setParameters(serializeObject(request.getParameters()));
            }
            
            auditLogMapper.insert(auditLog);
            logger.debug("Logged request initiation for requestId: {}", requestId);
            
        } catch (Exception e) {
            logger.error("Failed to log request for requestId: {}", requestId, e);
        }
    }
    
    /**
     * Log response completion
     */
    @Async
    public void logResponse(String requestId, EsbResponse response, long processingTime) {
        try {
            LocalDateTime responseTime = LocalDateTime.now();
            String responsePayload = null;
            
            // Serialize response data
            if (response.getData() != null) {
                responsePayload = serializeObject(response.getData());
            }
            
            auditLogMapper.updateResponse(
                requestId,
                responsePayload,
                response.getStatus(),
                response.getStatusCode(),
                response.getErrorCode(),
                response.getErrorMessage(),
                processingTime,
                responseTime
            );
            
            logger.debug("Logged response completion for requestId: {}", requestId);
            
        } catch (Exception e) {
            logger.error("Failed to log response for requestId: {}", requestId, e);
        }
    }
    
    /**
     * Log error occurred during processing
     */
    @Async
    public void logError(String requestId, String errorCode, String errorMessage, Exception exception) {
        try {
            String additionalInfo = null;
            if (exception != null) {
                additionalInfo = "Exception: " + exception.getClass().getSimpleName() + 
                               ", Message: " + exception.getMessage();
            }
            
            auditLogMapper.updateResponse(
                requestId,
                null,
                "ERROR",
                "500",
                errorCode,
                errorMessage,
                0,
                LocalDateTime.now()
            );
            
            // Update additional info if exception details available
            if (additionalInfo != null) {
                AuditLog auditLog = auditLogMapper.findByRequestId(requestId);
                if (auditLog != null) {
                    auditLog.setAdditionalInfo(additionalInfo);
                    auditLogMapper.update(auditLog);
                }
            }
            
            logger.debug("Logged error for requestId: {}", requestId);
            
        } catch (Exception e) {
            logger.error("Failed to log error for requestId: {}", requestId, e);
        }
    }
    
    /**
     * Get audit log by request ID
     */
    public AuditLog getAuditLog(String requestId) {
        return auditLogMapper.findByRequestId(requestId);
    }
    
    /**
     * Get audit logs by correlation ID
     */
    public List<AuditLog> getAuditLogsByCorrelation(String correlationId) {
        return auditLogMapper.findByCorrelationId(correlationId);
    }
    
    /**
     * Get audit logs by branch with pagination
     */
    public List<AuditLog> getAuditLogsByBranch(String branchCode, int page, int size) {
        int offset = page * size;
        return auditLogMapper.findByBranchCode(branchCode, offset, size);
    }
    
    /**
     * Get audit logs by endpoint with pagination
     */
    public List<AuditLog> getAuditLogsByEndpoint(String endpoint, int page, int size) {
        int offset = page * size;
        return auditLogMapper.findByEndpoint(endpoint, offset, size);
    }
    
    /**
     * Get audit logs by date range with pagination
     */
    public List<AuditLog> getAuditLogsByDateRange(LocalDateTime startDate, LocalDateTime endDate, 
                                                 int page, int size) {
        int offset = page * size;
        return auditLogMapper.findByDateRange(startDate, endDate, offset, size);
    }
    
    /**
     * Get failed transactions with pagination
     */
    public List<AuditLog> getFailedTransactions(int page, int size) {
        int offset = page * size;
        return auditLogMapper.findFailedTransactions(offset, size);
    }
    
    /**
     * Get recent audit logs
     */
    public List<AuditLog> getRecentAuditLogs(int limit) {
        return auditLogMapper.findRecent(limit);
    }
    
    /**
     * Get audit statistics
     */
    public Map<String, Object> getAuditStatistics() {
        Map<String, Object> stats = new ConcurrentHashMap<>();
        
        stats.put("totalTransactions", auditLogMapper.countTotal());
        stats.put("successfulTransactions", auditLogMapper.countByStatus("SUCCESS"));
        stats.put("failedTransactions", auditLogMapper.countByStatus("ERROR"));
        stats.put("averageProcessingTime", auditLogMapper.getAverageProcessingTime());
        
        return stats;
    }
    
    /**
     * Get audit statistics by date range
     */
    public Map<String, Object> getAuditStatisticsByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        Map<String, Object> stats = new ConcurrentHashMap<>();
        
        stats.put("totalTransactions", auditLogMapper.countByDateRange(startDate, endDate));
        stats.put("processingStats", auditLogMapper.getProcessingStats(startDate, endDate));
        
        return stats;
    }
    
    /**
     * Get transaction count by status
     */
    public long getTransactionCount(String status) {
        return auditLogMapper.countByStatus(status);
    }
    
    /**
     * Search audit logs with multiple criteria
     */
    public List<AuditLog> searchAuditLogs(AuditSearchCriteria criteria) {
        // Implementation for complex search based on criteria
        if (criteria.getCorrelationId() != null) {
            return auditLogMapper.findByCorrelationId(criteria.getCorrelationId());
        } else if (criteria.getBranchCode() != null) {
            return auditLogMapper.findByBranchCode(criteria.getBranchCode(), 
                                                  criteria.getOffset(), criteria.getLimit());
        } else if (criteria.getEndpoint() != null) {
            return auditLogMapper.findByEndpoint(criteria.getEndpoint(), 
                                                criteria.getOffset(), criteria.getLimit());
        } else if (criteria.getStartDate() != null && criteria.getEndDate() != null) {
            return auditLogMapper.findByDateRange(criteria.getStartDate(), criteria.getEndDate(), 
                                                 criteria.getOffset(), criteria.getLimit());
        } else {
            return auditLogMapper.findRecent(criteria.getLimit());
        }
    }
    
    /**
     * Cleanup old audit logs
     */
    public int cleanupOldAuditLogs(int daysToKeep) {
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(daysToKeep);
        int deletedCount = auditLogMapper.deleteOldLogs(cutoffDate);
        
        logger.info("Cleaned up {} audit logs older than {} days", deletedCount, daysToKeep);
        return deletedCount;
    }
    
    /**
     * Export audit logs for reporting
     */
    public List<AuditLog> exportAuditLogs(LocalDateTime startDate, LocalDateTime endDate) {
        return auditLogMapper.findByDateRange(startDate, endDate, 0, Integer.MAX_VALUE);
    }
    
    /**
     * Get performance metrics
     */
    public Map<String, Object> getPerformanceMetrics(LocalDateTime startDate, LocalDateTime endDate) {
        Map<String, Object> metrics = new ConcurrentHashMap<>();
        
        List<Object> processingStats = auditLogMapper.getProcessingStats(startDate, endDate);
        metrics.put("processingStats", processingStats);
        metrics.put("averageProcessingTime", auditLogMapper.getAverageProcessingTime());
        metrics.put("totalTransactions", auditLogMapper.countByDateRange(startDate, endDate));
        
        return metrics;
    }
    
    /**
     * Serialize object to JSON string
     */
    private String serializeObject(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            logger.warn("Failed to serialize object: {}", obj.getClass().getSimpleName(), e);
            return obj.toString();
        }
    }
    
    /**
     * Inner class for audit search criteria
     */
    public static class AuditSearchCriteria {
        private String correlationId;
        private String branchCode;
        private String endpoint;
        private String status;
        private String pluginId;
        private LocalDateTime startDate;
        private LocalDateTime endDate;
        private int offset = 0;
        private int limit = 100;
        
        // Getters and setters
        public String getCorrelationId() { return correlationId; }
        public void setCorrelationId(String correlationId) { this.correlationId = correlationId; }
        
        public String getBranchCode() { return branchCode; }
        public void setBranchCode(String branchCode) { this.branchCode = branchCode; }
        
        public String getEndpoint() { return endpoint; }
        public void setEndpoint(String endpoint) { this.endpoint = endpoint; }
        
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        
        public String getPluginId() { return pluginId; }
        public void setPluginId(String pluginId) { this.pluginId = pluginId; }
        
        public LocalDateTime getStartDate() { return startDate; }
        public void setStartDate(LocalDateTime startDate) { this.startDate = startDate; }
        
        public LocalDateTime getEndDate() { return endDate; }
        public void setEndDate(LocalDateTime endDate) { this.endDate = endDate; }
        
        public int getOffset() { return offset; }
        public void setOffset(int offset) { this.offset = offset; }
        
        public int getLimit() { return limit; }
        public void setLimit(int limit) { this.limit = limit; }
    }
}