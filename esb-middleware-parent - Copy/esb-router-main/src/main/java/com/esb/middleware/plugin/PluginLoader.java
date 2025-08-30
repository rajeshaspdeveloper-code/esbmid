package com.esb.middleware.plugin;

import com.esb.middleware.plugin.*;
import com.esb.plugin.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.concurrent.ConcurrentHashMap;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

/**
 * Plugin loader for dynamically loading JAR files
 */
@Component
public class PluginLoader {
    
    private static final Logger logger = LoggerFactory.getLogger(PluginLoader.class);
    
    private final ConcurrentHashMap<String, URLClassLoader> classLoaders = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, EsbPlugin> loadedPlugins = new ConcurrentHashMap<>();
    
    /**
     * Load plugin from JAR file
     */
    public EsbPlugin loadPlugin(String pluginId, File jarFile, String mainClassName) throws PluginException {
        try {
            logger.info("Loading plugin: {} from JAR: {}", pluginId, jarFile.getName());
            
            // Create class loader for the plugin
            URL[] urls = {jarFile.toURI().toURL()};
            URLClassLoader classLoader = new URLClassLoader(urls, this.getClass().getClassLoader());
            
            // Load the main plugin class
            Class<?> pluginClass = classLoader.loadClass(mainClassName);
            
            // Verify it implements EsbPlugin interface
            if (!EsbPlugin.class.isAssignableFrom(pluginClass)) {
                throw new PluginException(pluginId, "INVALID_PLUGIN", 
                    "Class " + mainClassName + " does not implement EsbPlugin interface");
            }
            
            // Create plugin instance
            EsbPlugin plugin = (EsbPlugin) pluginClass.getDeclaredConstructor().newInstance();
            
            // Store references
            classLoaders.put(pluginId, classLoader);
            loadedPlugins.put(pluginId, plugin);
            
            logger.info("Successfully loaded plugin: {}", pluginId);
            return plugin;
            
        } catch (Exception e) {
            logger.error("Failed to load plugin: {}", pluginId, e);
            throw new PluginException(pluginId, "LOAD_FAILED", 
                "Failed to load plugin: " + e.getMessage(), e);
        }
    }
    
    /**
     * Unload plugin and cleanup resources
     */
    public void unloadPlugin(String pluginId) throws PluginException {
        try {
            logger.info("Unloading plugin: {}", pluginId);
            
            // Get plugin instance and cleanup
            EsbPlugin plugin = loadedPlugins.get(pluginId);
            if (plugin != null) {
                plugin.destroy();
                loadedPlugins.remove(pluginId);
            }
            
            // Close class loader
            URLClassLoader classLoader = classLoaders.get(pluginId);
            if (classLoader != null) {
                classLoader.close();
                classLoaders.remove(pluginId);
            }
            
            logger.info("Successfully unloaded plugin: {}", pluginId);
            
        } catch (Exception e) {
            logger.error("Failed to unload plugin: {}", pluginId, e);
            throw new PluginException(pluginId, "UNLOAD_FAILED", 
                "Failed to unload plugin: " + e.getMessage(), e);
        }
    }
    
    /**
     * Reload plugin - unload and load again
     */
    public EsbPlugin reloadPlugin(String pluginId, File jarFile, String mainClassName) throws PluginException {
        logger.info("Reloading plugin: {}", pluginId);
        
        // Unload existing plugin
        if (isPluginLoaded(pluginId)) {
            unloadPlugin(pluginId);
        }
        
        // Load plugin again
        return loadPlugin(pluginId, jarFile, mainClassName);
    }
    
    /**
     * Get loaded plugin instance
     */
    public EsbPlugin getPlugin(String pluginId) {
        return loadedPlugins.get(pluginId);
    }
    
    /**
     * Check if plugin is loaded
     */
    public boolean isPluginLoaded(String pluginId) {
        return loadedPlugins.containsKey(pluginId);
    }
    
    /**
     * Get all loaded plugin IDs
     */
    public java.util.Set<String> getLoadedPluginIds() {
        return loadedPlugins.keySet();
    }
    
    /**
     * Get plugin class loader
     */
    public URLClassLoader getPluginClassLoader(String pluginId) {
        return classLoaders.get(pluginId);
    }
    
    /**
     * Extract plugin metadata from JAR manifest
     */
    public java.util.Map<String, String> extractPluginMetadata(File jarFile) throws PluginException {
        java.util.Map<String, String> metadata = new java.util.HashMap<>();
        
        try (JarFile jar = new JarFile(jarFile)) {
            Manifest manifest = jar.getManifest();
            if (manifest != null) {
                manifest.getMainAttributes().forEach((key, value) -> {
                    String keyStr = key.toString();
                    if (keyStr.startsWith("Plugin-") || keyStr.startsWith("Implementation-")) {
                        metadata.put(keyStr, value.toString());
                    }
                });
            }
        } catch (Exception e) {
            logger.warn("Failed to extract metadata from JAR: {}", jarFile.getName(), e);
            throw new PluginException("METADATA_EXTRACTION_FAILED", 
                "Failed to extract plugin metadata: " + e.getMessage(), e);
        }
        
        return metadata;
    }
    
    /**
     * Validate JAR file
     */
    public boolean validateJarFile(File jarFile) {
        if (jarFile == null || !jarFile.exists() || !jarFile.isFile()) {
            return false;
        }
        
        if (!jarFile.getName().toLowerCase().endsWith(".jar")) {
            return false;
        }
        
        try (JarFile jar = new JarFile(jarFile)) {
            return true;
        } catch (Exception e) {
            logger.warn("Invalid JAR file: {}", jarFile.getName(), e);
            return false;
        }
    }
    
    /**
     * Get total number of loaded plugins
     */
    public int getLoadedPluginCount() {
        return loadedPlugins.size();
    }
    
    /**
     * Shutdown all plugins and cleanup resources
     */
    public void shutdown() {
        logger.info("Shutting down plugin loader and unloading all plugins");
        
     /*   for (String pluginId : java.util.ArrayList<>(loadedPlugins.keySet())) {
            try {
                unloadPlugin(pluginId);
            } catch (Exception e) {
                logger.error("Error unloading plugin during shutdown: {}", pluginId, e);
            }
        }*/
        
        logger.info("Plugin loader shutdown completed");
    }
}