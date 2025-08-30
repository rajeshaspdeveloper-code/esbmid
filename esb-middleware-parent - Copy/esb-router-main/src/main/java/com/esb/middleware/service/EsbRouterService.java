package com.esb.middleware.service;

import com.esb.middleware.model.EsbRequest;
import com.esb.middleware.model.EsbResponse;
import com.esb.middleware.model.RouteConfig;
import com.esb.plugin.EsbPlugin;
import com.esb.plugin.PluginRequest;
import com.esb.plugin.PluginResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

/**
 * Main ESB routing service for processing requests
 */
@Service
public class EsbRouterService {
    
    private static final Logger logger = LoggerFactory.getLogger(EsbRouterService.class);
    
    @Autowired
    private ConfigurationService configurationService;
    
    @Autowired
    private PluginManagerService pluginManagerService;
    
    @Autowired
    private AuditService auditService;
    
    @Autowired
    @Qualifier("pluginExecutor")
    private Executor pluginExecutor;
    
    /**
     * Process ESB request synchronously
     */
    public EsbResponse processRequest(EsbRequest request, String sourceIp) {
        String requestId = generateRequestId();
        long startTime = System.currentTimeMillis();
        
        logger.info("Processing request: requestId={}, branch={}, endpoint={}", 
                   requestId, request.getBranchCode(), request.getEndpoint());
        
        try {
            // Find route configuration
            RouteConfig routeConfig = configurationService.getRouteConfig(
                request.getBranchCode(), request.getEndpoint());
            
            if (routeConfig == null) {
                logger.warn("No route found for branch: {}, endpoint: {}", 
                          request.getBranchCode(), request.getEndpoint());
                
                EsbResponse response = EsbResponse.notFound(requestId, 
                    request.getBranchCode(), request.getEndpoint());
                
                auditService.logRequest(requestId, request, null, sourceIp);
                auditService.logResponse(requestId, response, 
                    System.currentTimeMillis() - startTime);
                
                return response;
            }
            
            // Get plugin for processing
            EsbPlugin plugin = pluginManagerService.getPluginForEndpoint(request.getEndpoint());
            if (plugin == null) {
                logger.error("No plugin found for endpoint: {}, pluginId: {}", 
                           request.getEndpoint(), routeConfig.getPluginId());
                
                EsbResponse response = EsbResponse.error(requestId, 
                    "PLUGIN_NOT_FOUND", "Plugin not available for processing");
                
                auditService.logRequest(requestId, request, routeConfig.getPluginId(), sourceIp);
                auditService.logResponse(requestId, response, 
                    System.currentTimeMillis() - startTime);
                
                return response;
            }
            
            // Log request initiation
            auditService.logRequest(requestId, request, routeConfig.getPluginId(), sourceIp);
            
            // Convert to plugin request
            PluginRequest pluginRequest = convertToPluginRequest(request, requestId, routeConfig);
            
            // Process request through plugin
            PluginResponse pluginResponse = plugin.process(pluginRequest);
            
            // Convert plugin response to ESB response
            EsbResponse response = convertToEsbResponse(pluginResponse, requestId);
            
            // Log response
            long processingTime = System.currentTimeMillis() - startTime;
            response.setProcessingTime(processingTime);
            auditService.logResponse(requestId, response, processingTime);
            
            logger.info("Successfully processed request: requestId={}, status={}, time={}ms", 
                       requestId, response.getStatus(), processingTime);
            
            return response;
            
        } catch (Exception e) {
         //   logger.error("Error getting route health: branch={}, endpoint={}", 
                //       branchCode, endpoint, e);
            EsbResponse response = EsbResponse.error(requestId, 
                    "PROCESSING_ERROR", "Internal processing error: " + e.getMessage());
         //   health.put("error", e.getMessage());
        }
		return null;
        
   //     return response;
    }
    
    /**
     * Get service statistics
     */
    public java.util.Map<String, Object> getServiceStatistics() {
        java.util.Map<String, Object> stats = new java.util.concurrent.ConcurrentHashMap<>();
        
        // Get configuration statistics
        stats.putAll(configurationService.getConfigurationStats());
        
        // Get plugin statistics
        stats.putAll(pluginManagerService.getPluginStats());
        
        // Get audit statistics
        stats.putAll(auditService.getAuditStatistics());
        
        return stats;
    }

    
    /**
     * Process ESB request asynchronously
     */
    public CompletableFuture<EsbResponse> processRequestAsync(EsbRequest request, String sourceIp) {
        return CompletableFuture.supplyAsync(() -> processRequest(request, sourceIp), pluginExecutor);
    }
    
    /**
     * Convert ESB request to plugin request
     */
    private PluginRequest convertToPluginRequest(EsbRequest esbRequest, String requestId, RouteConfig routeConfig) {
        PluginRequest pluginRequest = new PluginRequest(requestId, 
            esbRequest.getBranchCode(), esbRequest.getEndpoint());
        
        pluginRequest.setMethod(esbRequest.getMethod());
        pluginRequest.setPayload(esbRequest.getPayload());
        pluginRequest.setHeaders(esbRequest.getHeaders());
        pluginRequest.setParameters(esbRequest.getParameters());
        pluginRequest.setSourceSystem(esbRequest.getSourceSystem());
        pluginRequest.setCorrelationId(esbRequest.getCorrelationId());
        pluginRequest.setTimestamp(esbRequest.getTimestamp());
        
        // Add route configuration parameters
        if (routeConfig.getTargetUrl() != null) {
            pluginRequest.addParameter("targetUrl", routeConfig.getTargetUrl());
        }
        
        if (routeConfig.getTimeout() > 0) {
            pluginRequest.addParameter("timeout", String.valueOf(routeConfig.getTimeout()));
        }
        
        if (routeConfig.getRetryCount() > 0) {
            pluginRequest.addParameter("retryCount", String.valueOf(routeConfig.getRetryCount()));
        }
        
        // Add route headers if configured
        if (routeConfig.getHeaders() != null) {
            routeConfig.getHeaders().forEach(pluginRequest::addHeader);
        }
        
        // Add route parameters if configured
        if (routeConfig.getParameters() != null) {
            routeConfig.getParameters().forEach(pluginRequest::addParameter);
        }
        
        return pluginRequest;
    }
    
    /**
     * Convert plugin response to ESB response
     */
    private EsbResponse convertToEsbResponse(PluginResponse pluginResponse, String requestId) {
        EsbResponse esbResponse = new EsbResponse(requestId);
        
        esbResponse.setStatus(pluginResponse.getStatus());
        esbResponse.setStatusCode(pluginResponse.getStatusCode());
        esbResponse.setMessage(pluginResponse.getMessage());
        esbResponse.setData(pluginResponse.getData());
        esbResponse.setHeaders(pluginResponse.getHeaders());
        esbResponse.setMetadata(pluginResponse.getMetadata());
        esbResponse.setErrorCode(pluginResponse.getErrorCode());
        esbResponse.setErrorMessage(pluginResponse.getErrorMessage());
        esbResponse.setTimestamp(pluginResponse.getTimestamp());
        
        return esbResponse;
    }
    
    /**
     * Generate unique request ID
     */
    private String generateRequestId() {
        return "EPIXESB-" + UUID.randomUUID().toString();
    }
    
    /**
     * Validate request
     */
    public boolean isValidRequest(EsbRequest request) {
        if (request == null) {
            return false;
        }
        
        if (request.getBranchCode() == null || request.getBranchCode().trim().isEmpty()) {
            return false;
        }
        
        if (request.getEndpoint() == null || request.getEndpoint().trim().isEmpty()) {
            return false;
        }
        
        return true;
    }
    
    /**
     * Check if route is available for processing
     */
    public boolean isRouteAvailable(String branchCode, String endpoint) {
        try {
            RouteConfig routeConfig = configurationService.getRouteConfig(branchCode, endpoint);
            if (routeConfig == null || !routeConfig.isEnabled()) {
                return false;
            }
            
            EsbPlugin plugin = pluginManagerService.getPluginForEndpoint(endpoint);
            return plugin != null && plugin.isHealthy();
            
        } catch (Exception e) {
            logger.error("Error checking route availability: branch={}, endpoint={}", 
                       branchCode, endpoint, e);
            return false;
        }
    }
    
    /**
     * Get route health status
     */
    public java.util.Map<String, Object> getRouteHealth(String branchCode, String endpoint) {
        java.util.Map<String, Object> health = new java.util.concurrent.ConcurrentHashMap<>();
        
        try {
            RouteConfig routeConfig = configurationService.getRouteConfig(branchCode, endpoint);
            health.put("routeConfigured", routeConfig != null);
            health.put("routeEnabled", routeConfig != null && routeConfig.isEnabled());
            
            if (routeConfig != null) {
                health.put("pluginId", routeConfig.getPluginId());
                
                EsbPlugin plugin = pluginManagerService.getPluginForEndpoint(endpoint);
                health.put("pluginAvailable", plugin != null);
                health.put("pluginHealthy", plugin != null && plugin.isHealthy());
            }
            
        } catch (Exception e) {
      /*    logger.error("Error processing request: requestId={}", requestId, e);
            
            EsbResponse response = EsbResponse.error(requestId, 
                "PROCESSING_ERROR", "Internal processing error: " + e.getMessage());
            
            long processingTime = System.currentTimeMillis() - startTime;
            response.setProcessingTime(processingTime);
            
            auditService.logError(requestId, "PROCESSING_ERROR", e.getMessage(), e);
            
            return response;*/
        }
		return health;
    }
    }
