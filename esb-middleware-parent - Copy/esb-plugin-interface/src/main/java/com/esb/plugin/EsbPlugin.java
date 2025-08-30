package com.esb.plugin;

/**
 * Interface that all ESB plugins must implement.
 * This defines the contract for plugin execution and lifecycle management.
 */
public interface EsbPlugin {

    /**
     * Initialize the plugin with configuration parameters
     * @param config Plugin configuration
     * @throws PluginException if initialization fails
     */
    void initialize(PluginConfiguration config) throws PluginException;

    /**
     * Process the incoming request and return response
     * @param request The plugin request
     * @return The plugin response
     * @throws PluginException if processing fails
     */
    PluginResponse process(PluginRequest request) throws PluginException;

    /**
     * Get plugin metadata information
     * @return Plugin metadata
     */
    PluginMetadata getMetadata();

    /**
     * Check if plugin is healthy and ready to process requests
     * @return true if healthy, false otherwise
     */
    boolean isHealthy();

    /**
     * Cleanup resources when plugin is being unloaded
     */
    void destroy();

    /**
     * Get plugin version
     * @return Plugin version string
     */
    String getVersion();

    /**
     * Get supported endpoints by this plugin
     * @return Array of supported endpoints
     */
    String[] getSupportedEndpoints();
}