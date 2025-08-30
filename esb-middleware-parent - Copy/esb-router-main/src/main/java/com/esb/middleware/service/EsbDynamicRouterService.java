package com.esb.middleware.service;

import com.esb.middleware.model.*;
import com.esb.middleware.mapper.BranchServiceConfigMapper;
import com.esb.plugin.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.Map;
import java.util.List;

@Service
public class EsbDynamicRouterService {
    
    private static final Logger logger = LoggerFactory.getLogger(EsbDynamicRouterService.class);
    
    @Autowired
    private BranchServiceConfigService branchServiceConfigService;
    
    @Autowired
    private PluginManagerService pluginManagerService;
    
    @Autowired
    private AuditService auditService;
    
    @Autowired
    @Qualifier("pluginExecutor")
    private Executor pluginExecutor;
    
    /**
     * Process ESB request with dynamic routing
     */
    public EsbResponse processRequest(String pluginId, String serviceEndpoint, String esbService,
                                    EsbRequestModel request, String sourceIp) {
        
        String requestId = generateRequestId();
        long startTime = System.currentTimeMillis();
        
        logger.info("Processing dynamic ESB request: requestId={}, pluginId={}, serviceEndpoint={}, " +
                   "esbService={}, branch={}, serviceId={}", 
                   requestId, pluginId, serviceEndpoint, esbService,
                   request.getEsbHeader().getBranch(), request.getEsbHeader().getServiceId());
        
        try {
            // Get branch service configuration
            BranchServiceConfig serviceConfig = branchServiceConfigService.getServiceConfig(
                request.getEsbHeader().getBranch(), request.getEsbHeader().getServiceId());
            
            if (serviceConfig == null) {
                logger.warn("No service configuration found for branch: {}, serviceId: {}", 
                          request.getEsbHeader().getBranch(), request.getEsbHeader().getServiceId());
                
                EsbResponse response = EsbResponse.notFound(requestId, 
                    request.getEsbHeader().getBranch(), request.getEsbHeader().getServiceId());
                
                auditService.logRequest(requestId, convertToEsbRequest(request), null, sourceIp);
                auditService.logResponse(requestId, response, System.currentTimeMillis() - startTime);
                
                return response;
            }
            
            // Use plugin from service config if provided, otherwise use path parameter
            String targetPluginId = serviceConfig.getPluginId() != null ? serviceConfig.getPluginId() : pluginId;
            
            // Get plugin for processing
            EsbPlugin plugin = pluginManagerService.getPlugin(targetPluginId);
            if (plugin == null || !plugin.isHealthy()) {
                logger.error("Plugin not found or unhealthy: {}", targetPluginId);
                
                EsbResponse response = EsbResponse.error(requestId, 
                    "PLUGIN_NOT_AVAILABLE", "Plugin not available: " + targetPluginId);
                
                auditService.logRequest(requestId, convertToEsbRequest(request), targetPluginId, sourceIp);
                auditService.logResponse(requestId, response, System.currentTimeMillis() - startTime);
                
                return response;
            }
            
            // Log request initiation
            auditService.logRequest(requestId, convertToEsbRequest(request), targetPluginId, sourceIp);
            
            // Convert to plugin request with service configuration
            PluginRequest pluginRequest = convertToPluginRequest(request, requestId, serviceConfig, 
                                                                serviceEndpoint, esbService);
            
            // Process request through plugin
            PluginResponse pluginResponse = plugin.process(pluginRequest);
            
            // Convert plugin response to ESB response
            EsbResponse response = convertToEsbResponse(pluginResponse, requestId);
            
            // Log response
            long processingTime = System.currentTimeMillis() - startTime;
            response.setProcessingTime(processingTime);
            auditService.logResponse(requestId, response, processingTime);
            
            logger.info("Successfully processed dynamic ESB request: requestId={}, status={}, time={}ms", 
                       requestId, response.getStatus(), processingTime);
            
            return response;
            
        } catch (Exception e) {
            logger.error("Error processing dynamic ESB request: requestId={}", requestId, e);
            
            EsbResponse response = EsbResponse.error(requestId, 
                "PROCESSING_ERROR", "Internal processing error: " + e.getMessage());
            
            long processingTime = System.currentTimeMillis() - startTime;
            response.setProcessingTime(processingTime);
            
            auditService.logError(requestId, "PROCESSING_ERROR", e.getMessage(), e);
            
            return response;
        }
    }
    
    /**
     * Process request asynchronously
     */
    public CompletableFuture<EsbResponse> processRequestAsync(String pluginId, String serviceEndpoint, 
                                                            String esbService, EsbRequestModel request, 
                                                            String sourceIp) {
        return CompletableFuture.supplyAsync(() -> 
            processRequest(pluginId, serviceEndpoint, esbService, request, sourceIp), pluginExecutor);
    }
    
    /**
     * Convert EsbRequestModel to PluginRequest
     */
    private PluginRequest convertToPluginRequest(EsbRequestModel esbRequest, String requestId,
                                               BranchServiceConfig serviceConfig, String serviceEndpoint,
                                               String esbService) {
        
        PluginRequest pluginRequest = new PluginRequest(requestId, 
            esbRequest.getEsbHeader().getBranch(), serviceEndpoint);
        
        pluginRequest.setMethod("POST");
        pluginRequest.setPayload(esbRequest);
        pluginRequest.setSourceSystem(esbRequest.getEsbHeader().getSourceApplication());
        pluginRequest.setCorrelationId(esbRequest.getEsbBody().getCorrelationId());
        
        // Add service configuration parameters
        if (serviceConfig.getTargetUrl() != null) {
            pluginRequest.addParameter("targetUrl", serviceConfig.getTargetUrl());
        }
        
        if (serviceConfig.getServiceEndpoint() != null) {
            pluginRequest.addParameter("serviceEndpoint", serviceConfig.getServiceEndpoint());
        }
        
        if (esbService != null) {
            pluginRequest.addParameter("esbService", esbService);
        } else if (serviceConfig.getEsbService() != null) {
            pluginRequest.addParameter("esbService", serviceConfig.getEsbService());
        }
        
        pluginRequest.addParameter("timeout", String.valueOf(serviceConfig.getTimeoutMs()));
        pluginRequest.addParameter("retryCount", String.valueOf(serviceConfig.getRetryCount()));
        pluginRequest.addParameter("serviceId", esbRequest.getEsbHeader().getServiceId());
        pluginRequest.addParameter("targetApplication", esbRequest.getEsbHeader().getTargetApplication());
        
        // Add configured headers
        if (serviceConfig.getHeaders() != null) {
            serviceConfig.getHeaders().forEach(pluginRequest::addHeader);
        }
        
        // Add configured parameters
        if (serviceConfig.getParameters() != null) {
            serviceConfig.getParameters().forEach(pluginRequest::addParameter);
        }
        
        return pluginRequest;
    }
    
    /**
     * Convert EsbRequestModel to legacy EsbRequest
     */
    private EsbRequest convertToEsbRequest(EsbRequestModel esbRequestModel) {
        EsbRequest esbRequest = new EsbRequest(
            esbRequestModel.getEsbHeader().getBranch(),
            esbRequestModel.getEsbHeader().getServiceId()
        );
        
        esbRequest.setPayload(esbRequestModel);
        esbRequest.setSourceSystem(esbRequestModel.getEsbHeader().getSourceApplication());
        esbRequest.setCorrelationId(esbRequestModel.getEsbBody().getCorrelationId());
        
        return esbRequest;
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
     * Validate request
     */
    public boolean isValidRequest(EsbRequestModel request) {
        if (request == null || request.getEsbHeader() == null) {
            return false;
        }
        
        if (request.getEsbHeader().getBranch() == null || 
            request.getEsbHeader().getBranch().trim().isEmpty()) {
            return false;
        }
        
        if (request.getEsbHeader().getServiceId() == null || 
            request.getEsbHeader().getServiceId().trim().isEmpty()) {
            return false;
        }
        
        return request.getEsbBody() != null;
    }
    
    /**
     * Get service health status
     */
    public Map<String, Object> getServiceHealth(String branchCode, String serviceId) {
        Map<String, Object> health = new java.util.concurrent.ConcurrentHashMap<>();
        
        try {
            BranchServiceConfig serviceConfig = branchServiceConfigService.getServiceConfig(branchCode, serviceId);
            health.put("serviceConfigured", serviceConfig != null);
            health.put("serviceEnabled", serviceConfig != null && serviceConfig.isEnabled());
            
            if (serviceConfig != null) {
                health.put("pluginId", serviceConfig.getPluginId());
                health.put("targetUrl", serviceConfig.getTargetUrl());
                
                EsbPlugin plugin = pluginManagerService.getPlugin(serviceConfig.getPluginId());
                health.put("pluginAvailable", plugin != null);
                health.put("pluginHealthy", plugin != null && plugin.isHealthy());
            }
            
        } catch (Exception e) {
            logger.error("Error getting service health: branch={}, serviceId={}", branchCode, serviceId, e);
            health.put("error", e.getMessage());
        }
        
        return health;
    }
    
    /**
     * Get available services for a branch
     */
    public List<BranchServiceConfig> getAvailableServices(String branchCode) {
        return branchServiceConfigService.getServicesByBranch(branchCode);
    }
    
    /**
     * Test service availability
     */
    public Map<String, Object> testService(String branchCode, String serviceId) {
        Map<String, Object> result = new java.util.concurrent.ConcurrentHashMap<>();
        
        try {
            BranchServiceConfig serviceConfig = branchServiceConfigService.getServiceConfig(branchCode, serviceId);
            boolean available = serviceConfig != null && serviceConfig.isEnabled();
            
            result.put("available", available);
            result.put("branchCode", branchCode);
            result.put("serviceId", serviceId);
            result.put("timestamp", java.time.LocalDateTime.now());
            
            if (available) {
                result.put("pluginId", serviceConfig.getPluginId());
                result.put("targetUrl", serviceConfig.getTargetUrl());
                result.put("serviceEndpoint", serviceConfig.getServiceEndpoint());
                result.put("esbService", serviceConfig.getEsbService());
                
                // Test plugin health
                EsbPlugin plugin = pluginManagerService.getPlugin(serviceConfig.getPluginId());
                result.put("pluginHealthy", plugin != null && plugin.isHealthy());
                result.put("status", "Service is available and ready");
            } else {
                result.put("status", "Service is not available");
            }
            
        } catch (Exception e) {
            logger.error("Error testing service: branch={}, serviceId={}", branchCode, serviceId, e);
            result.put("available", false);
            result.put("error", e.getMessage());
            result.put("timestamp", java.time.LocalDateTime.now());
        }
        
        return result;
    }
    
    private String generateRequestId() {
        return "EPIXESB-" + UUID.randomUUID().toString();
    }
}