package com.esb.middleware.plugin;

import com.esb.plugin.EsbPlugin;
import com.esb.plugin.PluginMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.Set;

/**
 * Plugin registry for managing loaded plugins and their metadata
 */
@Component
public class PluginRegistry {
    
    private static final Logger logger = LoggerFactory.getLogger(PluginRegistry.class);
    
    private final ConcurrentHashMap<String, EsbPlugin> plugins = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, PluginMetadata> pluginMetadata = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, List<String>> endpointToPluginMap = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Long> pluginLoadTimes = new ConcurrentHashMap<>();
    
    /**
     * Register a plugin in the registry
     */
    public void registerPlugin(String pluginId, EsbPlugin plugin) {
        logger.info("Registering plugin: {}", pluginId);
        
        plugins.put(pluginId, plugin);
        pluginLoadTimes.put(pluginId, System.currentTimeMillis());
        
        // Get and store plugin metadata
        PluginMetadata metadata = plugin.getMetadata();
        if (metadata != null) {
            pluginMetadata.put(pluginId, metadata);
            
            // Update endpoint mappings
            updateEndpointMappings(pluginId, metadata.getSupportedEndpoints());
        }
        
        logger.info("Successfully registered plugin: {} with {} endpoints", 
                   pluginId, metadata != null ? metadata.getSupportedEndpoints().size() : 0);
    }
    
    /**
     * Unregister a plugin from the registry
     */
    public void unregisterPlugin(String pluginId) {
        logger.info("Unregistering plugin: {}", pluginId);
        
        // Remove from main registry
        EsbPlugin plugin = plugins.remove(pluginId);
        PluginMetadata metadata = pluginMetadata.remove(pluginId);
        pluginLoadTimes.remove(pluginId);
        
        // Remove endpoint mappings
        if (metadata != null && metadata.getSupportedEndpoints() != null) {
            for (String endpoint : metadata.getSupportedEndpoints()) {
                List<String> pluginsForEndpoint = endpointToPluginMap.get(endpoint);
                if (pluginsForEndpoint != null) {
                    pluginsForEndpoint.remove(pluginId);
                    if (pluginsForEndpoint.isEmpty()) {
                        endpointToPluginMap.remove(endpoint);
                    }
                }
            }
        }
        
        logger.info("Successfully unregistered plugin: {}", pluginId);
    }
    
    /**
     * Get plugin by ID
     */
    public EsbPlugin getPlugin(String pluginId) {
        return plugins.get(pluginId);
    }
    
    /**
     * Get plugin metadata by ID
     */
    public PluginMetadata getPluginMetadata(String pluginId) {
        return pluginMetadata.get(pluginId);
    }
    
    /**
     * Find plugins that support a specific endpoint
     */
    public List<String> getPluginsForEndpoint(String endpoint) {
        return endpointToPluginMap.getOrDefault(endpoint, new ArrayList<>());
    }
    
    /**
     * Get the best plugin for an endpoint (based on priority)
     */
    public String getBestPluginForEndpoint(String endpoint) {
        List<String> plugins = getPluginsForEndpoint(endpoint);
        if (plugins.isEmpty()) {
            return null;
        }
        
        // Find plugin with highest priority
        String bestPlugin = null;
        int highestPriority = Integer.MIN_VALUE;
        
        for (String pluginId : plugins) {
            PluginMetadata metadata = pluginMetadata.get(pluginId);
            if (metadata != null && metadata.isEnabled() && metadata.getPriority() > highestPriority) {
                highestPriority = metadata.getPriority();
                bestPlugin = pluginId;
            }
        }
        
        return bestPlugin;
    }
    
    /**
     * Check if plugin is registered and healthy
     */
    public boolean isPluginHealthy(String pluginId) {
        EsbPlugin plugin = plugins.get(pluginId);
        return plugin != null && plugin.isHealthy();
    }
    
    /**
     * Get all registered plugin IDs
     */
    public Set<String> getAllPluginIds() {
        return plugins.keySet();
    }
    
    /**
     * Get all enabled plugin IDs
     */
    public List<String> getEnabledPluginIds() {
        List<String> enabledPlugins = new ArrayList<>();
        
        for (Map.Entry<String, PluginMetadata> entry : pluginMetadata.entrySet()) {
            if (entry.getValue().isEnabled()) {
                enabledPlugins.add(entry.getKey());
            }
        }
        
        return enabledPlugins;
    }
    
    /**
     * Get plugin load time
     */
    public Long getPluginLoadTime(String pluginId) {
        return pluginLoadTimes.get(pluginId);
    }
    
    /**
     * Get all supported endpoints
     */
    public Set<String> getAllSupportedEndpoints() {
        return endpointToPluginMap.keySet();
    }
    
    /**
     * Get registry statistics
     */
    public Map<String, Object> getRegistryStats() {
        Map<String, Object> stats = new ConcurrentHashMap<>();
        stats.put("totalPlugins", plugins.size());
        stats.put("enabledPlugins", getEnabledPluginIds().size());
        stats.put("supportedEndpoints", endpointToPluginMap.size());
        stats.put("healthyPlugins", getHealthyPluginCount());
        return stats;
    }
    
    /**
     * Get number of healthy plugins
     */
    private int getHealthyPluginCount() {
        int count = 0;
        for (String pluginId : plugins.keySet()) {
            if (isPluginHealthy(pluginId)) {
                count++;
            }
        }
        return count;
    }
    
    /**
     * Update endpoint mappings for a plugin
     */
    private void updateEndpointMappings(String pluginId, List<String> endpoints) {
        if (endpoints == null || endpoints.isEmpty()) {
            return;
        }
        
        for (String endpoint : endpoints) {
            endpointToPluginMap.computeIfAbsent(endpoint, k -> new ArrayList<>()).add(pluginId);
        }
    }
    
    /**
     * Perform health check on all plugins
     */
    public Map<String, Boolean> performHealthCheck() {
        Map<String, Boolean> healthStatus = new ConcurrentHashMap<>();
        
        for (String pluginId : plugins.keySet()) {
            try {
                boolean healthy = isPluginHealthy(pluginId);
                healthStatus.put(pluginId, healthy);
                
                if (!healthy) {
                    logger.warn("Plugin {} failed health check", pluginId);
                }
            } catch (Exception e) {
                logger.error("Error during health check for plugin: {}", pluginId, e);
                healthStatus.put(pluginId, false);
            }
        }
        
        return healthStatus;
    }
    
    /**
     * Get detailed plugin information
     */
    public List<Map<String, Object>> getDetailedPluginInfo() {
        List<Map<String, Object>> pluginInfo = new ArrayList<>();
        
        for (String pluginId : plugins.keySet()) {
            Map<String, Object> info = new ConcurrentHashMap<>();
            PluginMetadata metadata = pluginMetadata.get(pluginId);
            
            info.put("pluginId", pluginId);
            info.put("healthy", isPluginHealthy(pluginId));
            info.put("loadTime", pluginLoadTimes.get(pluginId));
            
            if (metadata != null) {
                info.put("name", metadata.getName());
                info.put("version", metadata.getVersion());
                info.put("description", metadata.getDescription());
                info.put("author", metadata.getAuthor());
                info.put("enabled", metadata.isEnabled());
                info.put("priority", metadata.getPriority());
                info.put("supportedEndpoints", metadata.getSupportedEndpoints());
            }
            
            pluginInfo.add(info);
        }
        
        return pluginInfo;
    }
    
    /**
     * Clear all plugins from registry
     */
    public void clear() {
        logger.info("Clearing plugin registry");
        plugins.clear();
        pluginMetadata.clear();
        endpointToPluginMap.clear();
        pluginLoadTimes.clear();
    }
}