package com.esb.middleware.service;

import com.esb.middleware.mapper.PluginConfigMapper;
import com.esb.middleware.model.PluginConfigModel;
import com.esb.middleware.plugin.PluginLoader;
import com.esb.middleware.plugin.PluginRegistry;
import com.esb.plugin.EsbPlugin;
import com.esb.plugin.PluginConfiguration;
import com.esb.plugin.PluginException;
import com.esb.plugin.PluginMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.File;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Service for managing plugin lifecycle and operations
 */
@Service
public class PluginManagerService {
    
    private static final Logger logger = LoggerFactory.getLogger(PluginManagerService.class);
    
    @Autowired
    private PluginLoader pluginLoader;
    
    @Autowired
    private PluginRegistry pluginRegistry;
    
    @Autowired
    private PluginConfigMapper pluginConfigMapper;
    
    @Value("${esb.plugin.directory:./plugins}")
    private String pluginDirectory;
    
    @Value("${esb.plugin.reload.enabled:true}")
    private boolean reloadEnabled;
    
    private final Map<String, Long> jarFileModifiedTimes = new ConcurrentHashMap<>();
    
    /**
     * Initialize and load all plugins from database configuration
     */
    @PostConstruct
    public void initializePlugins() {
        logger.info("Initializing plugin manager and loading plugins from database");
        
        try {
            List<PluginConfigModel> pluginConfigs = pluginConfigMapper.findAllEnabled();
            logger.info("Found {} enabled plugins in database", pluginConfigs.size());
            
            for (PluginConfigModel config : pluginConfigs) {
                try {
                    loadPluginFromConfig(config);
                } catch (Exception e) {
                    logger.error("Failed to load plugin: {}", config.getPluginId(), e);
                    // Update plugin status to ERROR
                    pluginConfigMapper.updateStatus(config.getPluginId(), false, "ERROR");
                }
            }
            
            logger.info("Plugin initialization completed. Loaded {} plugins", 
                       pluginRegistry.getAllPluginIds().size());
                       
        } catch (Exception e) {
            logger.error("Error during plugin initialization", e);
        }
    }
    
    /**
     * Load plugin from configuration
     */
    public void loadPluginFromConfig(PluginConfigModel config) throws PluginException {
        logger.info("Loading plugin: {} from config", config.getPluginId());
        
        File pluginDir = new File(pluginDirectory);
        File jarFile = new File(pluginDir, config.getJarFileName());
        
        if (!jarFile.exists()) {
            throw new PluginException(config.getPluginId(), "JAR_NOT_FOUND", 
                "JAR file not found: " + jarFile.getAbsolutePath());
        }
        
        // Load plugin using plugin loader
        EsbPlugin plugin = pluginLoader.loadPlugin(config.getPluginId(), jarFile, config.getMainClass());
        
        // Initialize plugin with configuration
        PluginConfiguration pluginConfig = createPluginConfiguration(config);
        plugin.initialize(pluginConfig);
        
        // Register plugin in registry
        pluginRegistry.registerPlugin(config.getPluginId(), plugin);
        
        // Update load time and status in database
        pluginConfigMapper.updateLoadedTime(config.getPluginId(), LocalDateTime.now());
        pluginConfigMapper.updateStatus(config.getPluginId(), true, "LOADED");
        
        // Track JAR file modification time
        jarFileModifiedTimes.put(config.getPluginId(), jarFile.lastModified());
        
        logger.info("Successfully loaded and initialized plugin: {}", config.getPluginId());
    }
    
    /**
     * Deploy new plugin from JAR file
     */
    public void deployPlugin(File jarFile, String pluginId, String mainClass) throws PluginException {
        logger.info("Deploying new plugin: {} from JAR: {}", pluginId, jarFile.getName());
        
        // Validate JAR file
        if (!pluginLoader.validateJarFile(jarFile)) {
            throw new PluginException(pluginId, "INVALID_JAR", "Invalid JAR file");
        }
        
        // Check if plugin already exists
        if (pluginConfigMapper.existsByPluginId(pluginId)) {
            throw new PluginException(pluginId, "PLUGIN_EXISTS", "Plugin already exists");
        }
        
        // Load plugin
        EsbPlugin plugin = pluginLoader.loadPlugin(pluginId, jarFile, mainClass);
        
        // Get plugin metadata
        PluginMetadata metadata = plugin.getMetadata();
        
        // Create plugin configuration in database
        PluginConfigModel config = new PluginConfigModel(pluginId, jarFile.getName(), mainClass);
        if (metadata != null) {
            config.setPluginName(metadata.getName());
            config.setVersion(metadata.getVersion());
            config.setDescription(metadata.getDescription());
            config.setAuthor(metadata.getAuthor());
            config.setVendor(metadata.getVendor());
            config.setPriority(metadata.getPriority());
            
            if (metadata.getSupportedEndpoints() != null) {
                config.setSupportedEndpoints(String.join(",", metadata.getSupportedEndpoints()));
            }
        }
        
        pluginConfigMapper.insert(config);
        
        // Initialize and register plugin
        PluginConfiguration pluginConfig = createPluginConfiguration(config);
        plugin.initialize(pluginConfig);
        pluginRegistry.registerPlugin(pluginId, plugin);
        
        jarFileModifiedTimes.put(pluginId, jarFile.lastModified());
        
        logger.info("Successfully deployed plugin: {}", pluginId);
    }
    
    /**
     * Undeploy plugin
     */
    public void undeployPlugin(String pluginId) throws PluginException {
        logger.info("Undeploying plugin: {}", pluginId);
        
        // Unregister from registry
        pluginRegistry.unregisterPlugin(pluginId);
        
        // Unload from plugin loader
        pluginLoader.unloadPlugin(pluginId);
        
        // Update database status
        pluginConfigMapper.updateStatus(pluginId, false, "UNLOADED");
        
        // Remove from tracking
        jarFileModifiedTimes.remove(pluginId);
        
        logger.info("Successfully undeployed plugin: {}", pluginId);
    }
    
    /**
     * Reload plugin
     */
    public void reloadPlugin(String pluginId) throws PluginException {
        logger.info("Reloading plugin: {}", pluginId);
        
        PluginConfigModel config = pluginConfigMapper.findByPluginId(pluginId);
        if (config == null) {
            throw new PluginException(pluginId, "CONFIG_NOT_FOUND", "Plugin configuration not found");
        }
        
        // Undeploy first
        if (pluginRegistry.getPlugin(pluginId) != null) {
            undeployPlugin(pluginId);
        }
        
        // Load again
        loadPluginFromConfig(config);
        
        logger.info("Successfully reloaded plugin: {}", pluginId);
    }
    
    /**
     * Enable/disable plugin
     */
    public void togglePlugin(String pluginId, boolean enabled) throws PluginException {
        logger.info("Toggling plugin: {} to {}", pluginId, enabled ? "enabled" : "disabled");
        
        PluginConfigModel config = pluginConfigMapper.findByPluginId(pluginId);
        if (config == null) {
            throw new PluginException(pluginId, "CONFIG_NOT_FOUND", "Plugin configuration not found");
        }
        
        if (enabled && pluginRegistry.getPlugin(pluginId) == null) {
            // Load plugin if enabling
            loadPluginFromConfig(config);
        } else if (!enabled && pluginRegistry.getPlugin(pluginId) != null) {
            // Unload plugin if disabling
            undeployPlugin(pluginId);
        }
        
        // Update database
        pluginConfigMapper.updateStatus(pluginId, enabled, enabled ? "LOADED" : "DISABLED");
        
        logger.info("Successfully toggled plugin: {} to {}", pluginId, enabled ? "enabled" : "disabled");
    }
    
    /**
     * Get plugin for processing request
     */
    public EsbPlugin getPluginForEndpoint(String endpoint) {
        String pluginId = pluginRegistry.getBestPluginForEndpoint(endpoint);
        if (pluginId != null) {
            return pluginRegistry.getPlugin(pluginId);
        }
        return null;
    }
    
    /**
     * Get all plugin information
     */
    public List<Map<String, Object>> getAllPluginInfo() {
        return pluginRegistry.getDetailedPluginInfo();
    }
    
    /**
     * Get plugin registry statistics
     */
    public Map<String, Object> getPluginStats() {
        return pluginRegistry.getRegistryStats();
    }
    
    /**
     * Perform health check on all plugins
     */
    public Map<String, Boolean> performHealthCheck() {
        return pluginRegistry.performHealthCheck();
    }
    
    /**
     * Scheduled method to check for plugin updates
     */
    @Scheduled(fixedDelayString = "${esb.plugin.reload.interval:300000}")
    public void checkForPluginUpdates() {
        if (!reloadEnabled) {
            return;
        }
        
        logger.debug("Checking for plugin updates");
        
        for (String pluginId : new ArrayList<>(jarFileModifiedTimes.keySet())) {
            try {
                PluginConfigModel config = pluginConfigMapper.findByPluginId(pluginId);
                if (config != null && config.isEnabled()) {
                    File jarFile = new File(new File(pluginDirectory), config.getJarFileName());
                    
                    if (jarFile.exists()) {
                        long lastModified = jarFile.lastModified();
                        Long trackedTime = jarFileModifiedTimes.get(pluginId);
                        
                        if (trackedTime != null && lastModified > trackedTime) {
                            logger.info("Detected update for plugin: {}, reloading", pluginId);
                            reloadPlugin(pluginId);
                        }
                    }
                }
            } catch (Exception e) {
                logger.error("Error checking update for plugin: {}", pluginId, e);
            }
        }
    }
    
    /**
     * Create plugin configuration from database config
     */
    private PluginConfiguration createPluginConfiguration(PluginConfigModel config) {
        PluginConfiguration pluginConfig = new PluginConfiguration(config.getPluginId());
        
        if (config.getConfiguration() != null) {
            pluginConfig.setProperties(config.getConfiguration());
        }
        
        // Add system properties
        pluginConfig.setProperty("plugin.directory", pluginDirectory);
        pluginConfig.setProperty("plugin.version", config.getVersion());
        pluginConfig.setProperty("plugin.jarFile", config.getJarFileName());
        
        return pluginConfig;
    }
    
    /**
     * Cleanup resources on shutdown
     */
    @PreDestroy
    public void shutdown() {
        logger.info("Shutting down plugin manager");
        
        try {
            pluginLoader.shutdown();
            pluginRegistry.clear();
            jarFileModifiedTimes.clear();
            
            logger.info("Plugin manager shutdown completed");
        } catch (Exception e) {
            logger.error("Error during plugin manager shutdown", e);
        }
    }

	public EsbPlugin getPlugin(String targetPluginId) {
		// TODO Auto-generated method stub
		return null;
	}
}