package com.esb.plugin.trade;

import com.esb.plugin.*;
import com.esb.plugin.trade.model.TradeRequest;
import com.esb.plugin.trade.model.TradeResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;

/**
 * Trade service for handling trade ESB operations
 */
public class TradeService {
    
    private static final Logger logger = LoggerFactory.getLogger(TradeService.class);
    
    private ObjectMapper objectMapper;
    private CloseableHttpClient httpClient;
    private PluginConfiguration configuration;
    private String baseUrl;
    private int timeout;
    private int retryCount;
    private boolean initialized = false;
    private boolean healthy = false;
    
    public void initialize(PluginConfiguration config) throws PluginException {
        logger.info("Initializing Trade Service");
        
        try {
            this.configuration = config;
            this.objectMapper = new ObjectMapper();
            this.httpClient = HttpClients.createDefault();
            
            // Get configuration parameters
            this.baseUrl = config.getProperty("targetUrl", "http://localhost:8081/trade");
            this.timeout = Integer.parseInt(config.getProperty("timeout", "30000"));
            this.retryCount = Integer.parseInt(config.getProperty("retryCount", "3"));
            
            this.initialized = true;
            this.healthy = true;
            
            logger.info("Trade Service initialized with baseUrl: {}, timeout: {}ms", baseUrl, timeout);
            
        } catch (Exception e) {
            logger.error("Failed to initialize Trade Service", e);
            this.healthy = false;
            throw new PluginException("trade-plugin", "SERVICE_INIT_FAILED", 
                "Failed to initialize Trade Service: " + e.getMessage(), e);
        }
    }
    
    /**
     * Process trade inquiry request
     */
    public PluginResponse processTradeInquiry(PluginRequest request) throws PluginException {
        logger.info("Processing trade inquiry: requestId={}", request.getRequestId());
        
        try {
            // Convert request payload to TradeRequest
            TradeRequest tradeRequest = convertToTradeRequest(request);
            
            // Validate trade inquiry request
            validateTradeInquiryRequest(tradeRequest);
            
            // Call external trade ESB system
            String targetUrl = request.getParameters().getOrDefault("targetUrl", baseUrl + "/inquiry").toString();
            TradeResponse tradeResponse = callTradeESB(targetUrl, tradeRequest, request.getRequestId());
            
            // Convert to plugin response
            return PluginResponse.success(request.getRequestId(), tradeResponse);
            
        } catch (PluginException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Error processing trade inquiry: requestId={}", request.getRequestId(), e);
            throw new PluginException("trade-plugin", "INQUIRY_FAILED", 
                "Trade inquiry processing failed: " + e.getMessage(), e);
        }
    }
    
    /**
     * Process trade booking request
     */
    public PluginResponse processTradeBooking(PluginRequest request) throws PluginException {
        logger.info("Processing trade booking: requestId={}", request.getRequestId());
        
        try {
            // Convert request payload to TradeRequest
            TradeRequest tradeRequest = convertToTradeRequest(request);
            
            // Validate trade booking request
            validateTradeBookingRequest(tradeRequest);
            
            // Call external trade ESB system
            String targetUrl = request.getParameters().getOrDefault("targetUrl", baseUrl + "/booking").toString();
            TradeResponse tradeResponse = callTradeESB(targetUrl, tradeRequest, request.getRequestId());
            
            // Convert to plugin response
            return PluginResponse.success(request.getRequestId(), tradeResponse);
            
        } catch (PluginException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Error processing trade booking: requestId={}", request.getRequestId(), e);
            throw new PluginException("trade-plugin", "BOOKING_FAILED", 
                "Trade booking processing failed: " + e.getMessage(), e);
        }
    }
    
    /**
     * Process trade confirmation request
     */
    public PluginResponse processTradeConfirmation(PluginRequest request) throws PluginException {
        logger.info("Processing trade confirmation: requestId={}", request.getRequestId());
        
        try {
            // Convert request payload to TradeRequest
            TradeRequest tradeRequest = convertToTradeRequest(request);
            
            // Validate trade confirmation request
            validateTradeConfirmationRequest(tradeRequest);
            
            // Call external trade ESB system
            String targetUrl = request.getParameters().getOrDefault("targetUrl", baseUrl + "/confirmation").toString();
            TradeResponse tradeResponse = callTradeESB(targetUrl, tradeRequest, request.getRequestId());
            
            // Convert to plugin response
            return PluginResponse.success(request.getRequestId(), tradeResponse);
            
        } catch (PluginException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Error processing trade confirmation: requestId={}", request.getRequestId(), e);
            throw new PluginException("trade-plugin", "CONFIRMATION_FAILED", 
                "Trade confirmation processing failed: " + e.getMessage(), e);
        }
    }
    
    /**
     * Call external trade ESB system
     */
    private TradeResponse callTradeESB(String url, TradeRequest tradeRequest, String requestId) throws PluginException {
        int attempts = 0;
        Exception lastException = null;
        
        while (attempts < retryCount) {
            attempts++;
            
            try {
                logger.debug("Calling trade ESB: url={}, attempt={}", url, attempts);
                
                HttpPost httpPost = new HttpPost(url);
                httpPost.setHeader("Content-Type", "application/json");
                httpPost.setHeader("X-Request-ID", requestId);
                
                // Serialize request
                String requestJson = objectMapper.writeValueAsString(tradeRequest);
                httpPost.setEntity(new StringEntity(requestJson, StandardCharsets.UTF_8));
                
                try (CloseableHttpResponse response = httpClient.execute(httpPost)) {
                    int statusCode = response.getStatusLine().getStatusCode();
                    String responseBody = EntityUtils.toString(response.getEntity());
                    
                    if (statusCode >= 200 && statusCode < 300) {
                        // Success response
                        TradeResponse tradeResponse = objectMapper.readValue(responseBody, TradeResponse.class);
                        logger.debug("Trade ESB call successful: requestId={}, attempt={}", requestId, attempts);
                        return tradeResponse;
                    } else {
                        // Error response
                        logger.warn("Trade ESB returned error: statusCode={}, body={}", statusCode, responseBody);
                        throw new PluginException("trade-plugin", "ESB_ERROR", 
                            "Trade ESB returned error: " + statusCode + " - " + responseBody);
                    }
                }
                
            } catch (PluginException e) {
                throw e; // Don't retry plugin exceptions
            } catch (Exception e) {
                logger.warn("Trade ESB call failed, attempt {}/{}: {}", attempts, retryCount, e.getMessage());
                lastException = e;
                
                if (attempts < retryCount) {
                    try {
                        Thread.sleep(1000 * attempts); // Exponential backoff
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            }
        }
        
        // All attempts failed
        throw new PluginException("trade-plugin", "ESB_CALL_FAILED", 
            "All " + retryCount + " attempts to call trade ESB failed. Last error: " + 
            (lastException != null ? lastException.getMessage() : "Unknown error"), lastException);
    }
    
    /**
     * Convert plugin request to trade request
     */
    private TradeRequest convertToTradeRequest(PluginRequest request) throws PluginException {
        try {
            if (request.getPayload() == null) {
                throw new PluginException("trade-plugin", "INVALID_PAYLOAD", "Request payload is null");
            }
            
            // Convert payload to TradeRequest
            String payloadJson = objectMapper.writeValueAsString(request.getPayload());
            TradeRequest tradeRequest = objectMapper.readValue(payloadJson, TradeRequest.class);
            
            // Set additional fields from request context
            tradeRequest.setRequestId(request.getRequestId());
            tradeRequest.setBranchCode(request.getBranchCode());
            tradeRequest.setCorrelationId(request.getCorrelationId());
            tradeRequest.setSourceSystem(request.getSourceSystem());
            
            return tradeRequest;
            
        } catch (Exception e) {
            logger.error("Failed to convert request payload to TradeRequest", e);
            throw new PluginException("trade-plugin", "PAYLOAD_CONVERSION_FAILED", 
                "Failed to convert request payload: " + e.getMessage(), e);
        }
    }
    
    /**
     * Validate trade inquiry request
     */
    private void validateTradeInquiryRequest(TradeRequest request) throws PluginException {
        if (request.getTradeId() == null || request.getTradeId().trim().isEmpty()) {
            throw new PluginException("trade-plugin", "VALIDATION_FAILED", 
                "Trade ID is required for inquiry");
        }
    }
    
    /**
     * Validate trade booking request
     */
    private void validateTradeBookingRequest(TradeRequest request) throws PluginException {
        if (request.getCustomerId() == null || request.getCustomerId().trim().isEmpty()) {
            throw new PluginException("trade-plugin", "VALIDATION_FAILED", 
                "Customer ID is required for booking");
        }
        
        if (request.getInstrument() == null || request.getInstrument().trim().isEmpty()) {
            throw new PluginException("trade-plugin", "VALIDATION_FAILED", 
                "Instrument is required for booking");
        }
        
        if (request.getQuantity() == null || request.getQuantity() <= 0) {
            throw new PluginException("trade-plugin", "VALIDATION_FAILED", 
                "Valid quantity is required for booking");
        }
    }
    
    /**
     * Validate trade confirmation request
     */
    private void validateTradeConfirmationRequest(TradeRequest request) throws PluginException {
        if (request.getTradeId() == null || request.getTradeId().trim().isEmpty()) {
            throw new PluginException("trade-plugin", "VALIDATION_FAILED", 
                "Trade ID is required for confirmation");
        }
        
        if (request.getConfirmationAction() == null || request.getConfirmationAction().trim().isEmpty()) {
            throw new PluginException("trade-plugin", "VALIDATION_FAILED", 
                "Confirmation action is required");
        }
    }
    
    /**
     * Check if service is healthy
     */
    public boolean isHealthy() {
        try {
            // Perform a simple health check - could be a ping to the ESB system
            // For now, just check if HTTP client is available
            return initialized && healthy && httpClient != null && configuration != null;
        } catch (Exception e) {
            logger.error("Trade service health check failed", e);
            return false;
        }
    }
    
    /**
     * Cleanup resources
     */
    public void cleanup() {
        logger.info("Cleaning up Trade Service");
        
        try {
            if (httpClient != null) {
                httpClient.close();
            }
            
            this.initialized = false;
            this.healthy = false;
            
            logger.info("Trade Service cleanup completed");
            
        } catch (Exception e) {
            logger.error("Error during Trade Service cleanup", e);
        }
    }
}