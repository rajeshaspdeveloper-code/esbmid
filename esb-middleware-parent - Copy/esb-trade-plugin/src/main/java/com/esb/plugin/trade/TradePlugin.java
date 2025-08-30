package com.esb.plugin.trade;

import com.esb.plugin.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;

/**
 * Trade ESB Plugin implementation
 */
public class TradePlugin implements EsbPlugin {

    private static final Logger logger = LoggerFactory.getLogger(TradePlugin.class);

    private PluginConfiguration configuration;
    private TradeService tradeService;
    private boolean initialized = false;
    private boolean healthy = false;

    @Override
    public void initialize(PluginConfiguration config) throws PluginException {
        logger.info("Initializing Trade Plugin with config: {}", config);

        try {
            this.configuration = config;
            this.tradeService = new TradeService();
            this.tradeService.initialize(config);
            this.initialized = true;
            this.healthy = true;

            logger.info("Trade Plugin initialized successfully");

        } catch (Exception e) {
            logger.error("Failed to initialize Trade Plugin", e);
            this.healthy = false;
            throw new PluginException("trade-plugin", "INIT_FAILED",
                    "Failed to initialize Trade Plugin: " + e.getMessage(), e);
        }
    }

    @Override
    public PluginResponse process(PluginRequest request) throws PluginException {
        if (!initialized || !healthy) {
            throw new PluginException("trade-plugin", "NOT_READY", "Plugin not initialized or unhealthy");
        }

        logger.info("Processing trade request: requestId={}, endpoint={}",
                request.getRequestId(), request.getEndpoint());

        long startTime = System.currentTimeMillis();

        try {
            PluginResponse response;

            switch (request.getEndpoint()) {
                case "trade-inquiry":
                    response = tradeService.processTradeInquiry(request);
                    break;
                case "trade-booking":
                    response = tradeService.processTradeBooking(request);
                    break;
                case "trade-confirmation":
                    response = tradeService.processTradeConfirmation(request);
                    break;
                default:
                    response = PluginResponse.error(request.getRequestId(),
                            "UNSUPPORTED_ENDPOINT", "Endpoint not supported: " + request.getEndpoint());
            }

            long processingTime = System.currentTimeMillis() - startTime;
            response.setProcessingTime(processingTime);

            logger.info("Trade request processed: requestId={}, status={}, time={}ms",
                    request.getRequestId(), response.getStatus(), processingTime);

            return response;

        } catch (Exception e) {
            logger.error("Error processing trade request: requestId={}", request.getRequestId(), e);

            PluginResponse response = PluginResponse.error(request.getRequestId(),
                    "PROCESSING_ERROR", "Trade processing failed: " + e.getMessage());
            response.setProcessingTime(System.currentTimeMillis() - startTime);

            return response;
        }
    }

    @Override
    public PluginMetadata getMetadata() {
        PluginMetadata metadata = new PluginMetadata("trade-plugin", "Trade ESB Plugin", "1.0.0");
        metadata.setDescription("Plugin for handling trade-related ESB operations");
        metadata.setAuthor("ESB Team");
        metadata.setVendor("YourCompany");
        metadata.setMainClass("com.esb.plugin.trade.TradePlugin");
        metadata.setJarFileName("esb-trade-plugin-1.0.0.jar");
        metadata.setPriority(10);
        metadata.setEnabled(true);

        // Set supported endpoints
        metadata.setSupportedEndpoints(Arrays.asList(
                "trade-inquiry", "trade-booking", "trade-confirmation"
        ));

        // Add configuration
        metadata.addConfiguration("timeout", "30000");
        metadata.addConfiguration("retryCount", "3");
        metadata.addConfiguration("batchSize", "100");

        return metadata;
    }

    @Override
    public boolean isHealthy() {
        if (!initialized) {
            return false;
        }

        try {
            // Perform health checks
         //   boolean serviceHealthy = tradeService != null && tradeService.isHealthy();

            // Check configuration
            boolean configValid = configuration != null;

         //   this.healthy = serviceHealthy && configValid;
            return this.healthy;

        } catch (Exception e) {
            logger.error("Health check failed for Trade Plugin", e);
            this.healthy = false;
            return false;
        }
    }

    @Override
    public void destroy() {
        logger.info("Destroying Trade Plugin");

        try {
            if (tradeService != null) {
         //       tradeService.cleanup();
            }

            this.initialized = false;
            this.healthy = false;

            logger.info("Trade Plugin destroyed successfully");

        } catch (Exception e) {
            logger.error("Error destroying Trade Plugin", e);
        }
    }

    @Override
    public String getVersion() {
        return "1.0.0";
    }

    @Override
    public String[] getSupportedEndpoints() {
        return new String[]{"trade-inquiry", "trade-booking", "trade-confirmation"};
    }
}