// ========================================================================
// File: esb-router-main/src/main/java/com/esb/middleware/controller/EsbDynamicRouterController.java
// REPLACE YOUR EXISTING CONTROLLER WITH THIS SIMPLIFIED VERSION
// ========================================================================
package com.esb.middleware.controller;

import com.esb.middleware.model.EsbRequest;
import com.esb.middleware.model.EsbResponse;
import com.esb.middleware.service.EsbRouterService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.Map;
import java.util.HashMap;

/**
 * ESB Router Controller - Simplified version that works with existing infrastructure
 */
@RestController
@RequestMapping("/api/esb")
@CrossOrigin(origins = "*")
public class EsbDynamicRouterController {
    
    private static final Logger logger = LoggerFactory.getLogger(EsbDynamicRouterController.class);
    
    @Autowired
    private EsbRouterService esbRouterService;
    
    /**
     * Legacy endpoint - works with your existing infrastructure
     */
    @PostMapping("/route")
    public ResponseEntity<EsbResponse> routeRequest(@Valid @RequestBody Object request,
                                                   HttpServletRequest httpRequest) {
        
        logger.info("Received ESB request on legacy endpoint");
        
        try {
            // Convert to your existing EsbRequest format
            EsbRequest esbRequest = convertToEsbRequest(request);
            
            // Validate request
            if (!esbRouterService.isValidRequest(esbRequest)) {
                EsbResponse response = EsbResponse.error("INVALID_REQUEST", 
                    "VALIDATION_ERROR", "Invalid request parameters");
                return ResponseEntity.badRequest().body(response);
            }
            
            String sourceIp = getClientIpAddress(httpRequest);
            
            // Process request using existing service
            EsbResponse response = esbRouterService.processRequest(esbRequest, sourceIp);
            
            HttpStatus httpStatus = mapResponseStatus(response);
            return ResponseEntity.status(httpStatus).body(response);
            
        } catch (Exception e) {
            logger.error("Error processing request", e);
            
            EsbResponse response = EsbResponse.error("SYSTEM_ERROR", 
                "INTERNAL_ERROR", "System error occurred: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    /**
     * NEW: Dynamic routing endpoint that forwards to existing infrastructure
     * This handles your new URL format: /route/{pluginId}/{serviceEndpoint}?esbService={serviceName}
     */
    @PostMapping("/route/{pluginId}/{serviceEndpoint}")
    public ResponseEntity<EsbResponse> routeDynamicRequest(
            @PathVariable String pluginId,
            @PathVariable String serviceEndpoint,
            @RequestParam(required = false) String esbService,
            @Valid @RequestBody Object request,
            HttpServletRequest httpRequest) {
        
        logger.info("Processing dynamic ESB request: pluginId={}, serviceEndpoint={}, esbService={}", 
                   pluginId, serviceEndpoint, esbService);
        
        try {
            // Extract branch and serviceId from request
            Map<String, Object> requestData = extractRequestData(request);
            String branchCode = (String) requestData.get("branch");
            String serviceId = (String) requestData.get("serviceId");
            
            logger.info("Extracted from request: branch={}, serviceId={}", branchCode, serviceId);
            
            // Convert to EsbRequest format
            EsbRequest esbRequest = convertToEsbRequest(request);
            
            // Override with extracted values
            if (branchCode != null) {
                esbRequest.setBranchCode(branchCode);
            }
            if (serviceEndpoint != null) {
                esbRequest.setEndpoint(serviceEndpoint);
            }
            
            // Add dynamic routing parameters
            if (esbRequest.getParameters() == null) {
                esbRequest.setParameters(new HashMap<>());
            }
            esbRequest.addParameter("pluginId", pluginId);
            esbRequest.addParameter("serviceEndpoint", serviceEndpoint);
            if (esbService != null) {
                esbRequest.addParameter("esbService", esbService);
            }
            if (serviceId != null) {
                esbRequest.addParameter("serviceId", serviceId);
            }
            
            String sourceIp = getClientIpAddress(httpRequest);
            
            // Process through existing router service
            EsbResponse response = esbRouterService.processRequest(esbRequest, sourceIp);
            
            HttpStatus httpStatus = mapResponseStatus(response);
            return ResponseEntity.status(httpStatus).body(response);
            
        } catch (Exception e) {
            logger.error("Error processing dynamic request", e);
            
            EsbResponse response = EsbResponse.error("SYSTEM_ERROR", 
                "INTERNAL_ERROR", "Dynamic routing error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    /**
     * Health check endpoint
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> healthCheck() {
        Map<String, Object> health = new HashMap<>();
        
        try {
            Map<String, Object> stats = esbRouterService.getServiceStatistics();
            health.put("status", "UP");
            health.put("statistics", stats);
            health.put("timestamp", java.time.LocalDateTime.now());
            
            return ResponseEntity.ok(health);
            
        } catch (Exception e) {
            logger.error("Health check failed", e);
            health.put("status", "DOWN");
            health.put("error", e.getMessage());
            health.put("timestamp", java.time.LocalDateTime.now());
            
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(health);
        }
    }
    
    /**
     * Get service statistics
     */
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getStatistics() {
        try {
            Map<String, Object> stats = esbRouterService.getServiceStatistics();
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            logger.error("Error getting statistics", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Extract branch and serviceId from the request payload
     */
    private Map<String, Object> extractRequestData(Object request) {
        Map<String, Object> data = new HashMap<>();
        
        try {
            if (request instanceof Map) {
                Map<?, ?> requestMap = (Map<?, ?>) request;
                
                // Try to extract esbHeader information
                Object esbHeader = requestMap.get("esbHeader");
                if (esbHeader instanceof Map) {
                    Map<?, ?> headerMap = (Map<?, ?>) esbHeader;
                    data.put("branch", headerMap.get("branch"));
                    data.put("serviceId", headerMap.get("serviceId"));
                    data.put("sourceApplication", headerMap.get("sourceApplication"));
                }
                
                // Try to extract esbBody information
                Object esbBody = requestMap.get("esbBody");
                if (esbBody instanceof Map) {
                    Map<?, ?> bodyMap = (Map<?, ?>) esbBody;
                    data.put("correlationId", bodyMap.get("correlationId"));
                }
            }
        } catch (Exception e) {
            logger.warn("Error extracting request data", e);
        }
        
        return data;
    }
    
    /**
     * Convert generic request to EsbRequest
     */
    private EsbRequest convertToEsbRequest(Object request) {
        EsbRequest esbRequest = new EsbRequest();
        
        // Set default values
        esbRequest.setBranchCode("DEFAULT");
        esbRequest.setEndpoint("default");
        esbRequest.setPayload(request);
        esbRequest.setTimestamp(java.time.LocalDateTime.now());
        
        // Try to extract specific values
        Map<String, Object> requestData = extractRequestData(request);
        
        if (requestData.get("branch") != null) {
            esbRequest.setBranchCode((String) requestData.get("branch"));
        }
        if (requestData.get("serviceId") != null) {
            esbRequest.setEndpoint((String) requestData.get("serviceId"));
        }
        if (requestData.get("sourceApplication") != null) {
            esbRequest.setSourceSystem((String) requestData.get("sourceApplication"));
        }
        if (requestData.get("correlationId") != null) {
            esbRequest.setCorrelationId((String) requestData.get("correlationId"));
        }
        
        return esbRequest;
    }
    
    /**
     * Map ESB response status to HTTP status
     */
    private HttpStatus mapResponseStatus(EsbResponse response) {
        if (response == null) {
            return HttpStatus.INTERNAL_SERVER_ERROR;
        }
        
        switch (response.getStatus()) {
            case "SUCCESS":
                return HttpStatus.OK;
            case "NOT_FOUND":
                return HttpStatus.NOT_FOUND;
            case "BUSINESS_ERROR":
                return HttpStatus.BAD_REQUEST;
            case "ERROR":
            default:
                return HttpStatus.INTERNAL_SERVER_ERROR;
        }
    }
    
    /**
     * Get client IP address from request
     */
    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }
        
        return request.getRemoteAddr();
    }
}

// ========================================================================
// File: Remove or comment out these problematic files:
// ========================================================================

// 1. DELETE OR RENAME: esb-router-main/src/main/java/com/esb/middleware/service/BranchServiceConfigService.java
// 2. DELETE OR RENAME: esb-router-main/src/main/java/com/esb/middleware/service/EsbDynamicRouterService.java  
// 3. DELETE OR RENAME: esb-router-main/src/main/java/com/esb/middleware/mapper/BranchServiceConfigMapper.java
// 4. DELETE OR RENAME: esb-router-main/src/main/resources/mappers/BranchServiceConfigMapper.xml