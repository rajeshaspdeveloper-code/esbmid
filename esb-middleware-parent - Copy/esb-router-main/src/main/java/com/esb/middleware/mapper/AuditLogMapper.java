package com.esb.middleware.mapper;

import com.esb.middleware.model.AuditLog;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

/**
 * MyBatis mapper for AuditLog operations
 */
@Mapper
public interface AuditLogMapper {
    
    /**
     * Insert new audit log entry
     */
    int insert(AuditLog auditLog);
    
    /**
     * Update audit log entry
     */
    int update(AuditLog auditLog);
    
    /**
     * Update audit log response information
     */
    int updateResponse(@Param("requestId") String requestId,
                      @Param("responsePayload") String responsePayload,
                      @Param("status") String status,
                      @Param("statusCode") String statusCode,
                      @Param("errorCode") String errorCode,
                      @Param("errorMessage") String errorMessage,
                      @Param("processingTime") long processingTime,
                      @Param("responseTime") LocalDateTime responseTime);
    
    /**
     * Find audit log by request ID
     */
    AuditLog findByRequestId(@Param("requestId") String requestId);
    
    /**
     * Find audit logs by correlation ID
     */
    List<AuditLog> findByCorrelationId(@Param("correlationId") String correlationId);
    
    /**
     * Find audit logs by branch code
     */
    List<AuditLog> findByBranchCode(@Param("branchCode") String branchCode, 
                                   @Param("offset") int offset, 
                                   @Param("limit") int limit);
    
    /**
     * Find audit logs by endpoint
     */
    List<AuditLog> findByEndpoint(@Param("endpoint") String endpoint, 
                                 @Param("offset") int offset, 
                                 @Param("limit") int limit);
    
    /**
     * Find audit logs by date range
     */
    List<AuditLog> findByDateRange(@Param("startDate") LocalDateTime startDate,
                                  @Param("endDate") LocalDateTime endDate,
                                  @Param("offset") int offset,
                                  @Param("limit") int limit);
    
    /**
     * Find audit logs by status
     */
    List<AuditLog> findByStatus(@Param("status") String status, 
                               @Param("offset") int offset, 
                               @Param("limit") int limit);
    
    /**
     * Find audit logs by plugin ID
     */
    List<AuditLog> findByPluginId(@Param("pluginId") String pluginId, 
                                 @Param("offset") int offset, 
                                 @Param("limit") int limit);
    
    /**
     * Find recent audit logs
     */
    List<AuditLog> findRecent(@Param("limit") int limit);
    
    /**
     * Find failed transactions
     */
    List<AuditLog> findFailedTransactions(@Param("offset") int offset, 
                                         @Param("limit") int limit);
    
    /**
     * Count total audit logs
     */
    long countTotal();
    
    /**
     * Count audit logs by status
     */
    long countByStatus(@Param("status") String status);
    
    /**
     * Count audit logs by date range
     */
    long countByDateRange(@Param("startDate") LocalDateTime startDate,
                         @Param("endDate") LocalDateTime endDate);
    
    /**
     * Get average processing time
     */
    Double getAverageProcessingTime();
    
    /**
     * Get processing statistics by date range
     */
    List<Object> getProcessingStats(@Param("startDate") LocalDateTime startDate,
                                   @Param("endDate") LocalDateTime endDate);
    
    /**
     * Delete old audit logs (for cleanup)
     */
    int deleteOldLogs(@Param("beforeDate") LocalDateTime beforeDate);
    
    /**
     * Find audit log by ID
     */
    AuditLog findById(@Param("id") Long id);
}