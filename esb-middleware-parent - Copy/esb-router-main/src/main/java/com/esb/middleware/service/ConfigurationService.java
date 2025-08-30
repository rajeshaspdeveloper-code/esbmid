package com.esb.middleware.service;

import com.esb.middleware.mapper.RouteConfigMapper;
import com.esb.middleware.model.RouteConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Service for managing ESB routing configuration
 */
@Service
public class ConfigurationService {
    
    private static final Logger logger = LoggerFactory.getLogger(ConfigurationService.class);
    
    @Autowired
    private RouteConfigMapper routeConfigMapper;
    
    // In-memory cache for fast route lookups
    private final Map<String, RouteConfig> routeCache = new ConcurrentHashMap<>();
    
    /**
     * Initialize configuration cache
     */
    @PostConstruct
    public void initializeCache() {
        logger.info("Initializing route configuration cache");
        refreshCache();
        logger.info("Route configuration cache initialized with {} routes", routeCache.size());
    }
    
    /**
     * Get route configuration for branch and endpoint
     */
    @Cacheable(value = "routeConfig", key = "#branchCode + '_' + #endpoint")
    public RouteConfig getRouteConfig(String branchCode, String endpoint) {
        String cacheKey = branchCode + "_" + endpoint;
        RouteConfig config = routeCache.get(cacheKey);
        
        if (config == null) {
            // Fetch from database if not in cache
            config = routeConfigMapper.findByBranchAndEndpoint(branchCode, endpoint);
            if (config != null && config.isEnabled()) {
                routeCache.put(cacheKey, config);
            }
        }
        
        return config != null && config.isEnabled() ? config : null;
    }
    
    /**
     * Get all route configurations for a branch
     */
    public List<RouteConfig> getRoutesByBranch(String branchCode) {
        return routeConfigMapper.findByBranch(branchCode);
    }
    
    /**
     * Get all enabled route configurations
     */
    public List<RouteConfig> getAllEnabledRoutes() {
        return routeConfigMapper.findAllEnabled();
    }
    
    /**
     * Create new route configuration
     */
    @CacheEvict(value = "routeConfig", allEntries = true)
    public RouteConfig createRoute(RouteConfig routeConfig) {
        logger.info("Creating new route: branch={}, endpoint={}, plugin={}", 
                   routeConfig.getBranchCode(), routeConfig.getEndpoint(), routeConfig.getPluginId());
        
        routeConfig.setCreatedDate(LocalDateTime.now());
        routeConfig.setModifiedDate(LocalDateTime.now());
        
        int result = routeConfigMapper.insert(routeConfig);
        if (result > 0) {
            refreshCache();
            logger.info("Successfully created route with ID: {}", routeConfig.getId());
            return routeConfig;
        } else {
            throw new RuntimeException("Failed to create route configuration");
        }
    }
    
    /**
     * Update existing route configuration
     */
    @CacheEvict(value = "routeConfig", allEntries = true)
    public RouteConfig updateRoute(RouteConfig routeConfig) {
        logger.info("Updating route: ID={}, branch={}, endpoint={}", 
                   routeConfig.getId(), routeConfig.getBranchCode(), routeConfig.getEndpoint());
        
        routeConfig.setModifiedDate(LocalDateTime.now());
        
        int result = routeConfigMapper.update(routeConfig);
        if (result > 0) {
            refreshCache();
            logger.info("Successfully updated route with ID: {}", routeConfig.getId());
            return routeConfig;
        } else {
            throw new RuntimeException("Failed to update route configuration");
        }
    }
    
    /**
     * Delete route configuration
     */
    @CacheEvict(value = "routeConfig", allEntries = true)
    public boolean deleteRoute(Long routeId) {
        logger.info("Deleting route with ID: {}", routeId);
        
        int result = routeConfigMapper.deleteById(routeId);
        if (result > 0) {
            refreshCache();
            logger.info("Successfully deleted route with ID: {}", routeId);
            return true;
        } else {
            logger.warn("Route not found for deletion: {}", routeId);
            return false;
        }
    }
    
    /**
     * Enable/disable route configuration
     */
    @CacheEvict(value = "routeConfig", allEntries = true)
    public boolean toggleRoute(Long routeId, boolean enabled) {
        logger.info("Toggling route: ID={}, enabled={}", routeId, enabled);
        
        int result = routeConfigMapper.updateStatus(routeId, enabled);
        if (result > 0) {
            refreshCache();
            logger.info("Successfully toggled route: ID={}, enabled={}", routeId, enabled);
            return true;
        } else {
            logger.warn("Route not found for toggle: {}", routeId);
            return false;
        }
    }
    
    /**
     * Get route configuration by ID
     */
    public RouteConfig getRouteById(Long routeId) {
        return routeConfigMapper.findById(routeId);
    }
    
    /**
     * Get routes by plugin ID
     */
    public List<RouteConfig> getRoutesByPlugin(String pluginId) {
        return routeConfigMapper.findByPluginId(pluginId);
    }
    
    /**
     * Get configuration statistics
     */
    public Map<String, Object> getConfigurationStats() {
        Map<String, Object> stats = new ConcurrentHashMap<>();
        stats.put("totalRoutes", routeConfigMapper.countTotal());
        stats.put("enabledRoutes", routeConfigMapper.countEnabled());
        stats.put("cachedRoutes", routeCache.size());
        return stats;
    }
    
    /**
     * Refresh the entire configuration cache
     */
    @CacheEvict(value = "routeConfig", allEntries = true)
    public void refreshCache() {
        logger.info("Refreshing route configuration cache");
        
        routeCache.clear();
        List<RouteConfig> allRoutes = routeConfigMapper.findAllEnabled();
        
        for (RouteConfig route : allRoutes) {
            String cacheKey = route.getBranchCode() + "_" + route.getEndpoint();
            routeCache.put(cacheKey, route);
        }
        
        logger.info("Route configuration cache refreshed with {} routes", routeCache.size());
    }
    
    /**
     * Validate route configuration
     */
    public boolean isValidRoute(String branchCode, String endpoint) {
        RouteConfig config = getRouteConfig(branchCode, endpoint);
        return config != null && config.isEnabled();
    }
    
    /**
     * Get all unique branch codes
     */
    public List<String> getAllBranches() {
        return routeConfigMapper.findAll().stream()
                .map(RouteConfig::getBranchCode)
                .distinct()
                .sorted()
                .collect(java.util.stream.Collectors.toList());
    }
    
    /**
     * Get all unique endpoints
     */
    public List<String> getAllEndpoints() {
        return routeConfigMapper.findAll().stream()
                .map(RouteConfig::getEndpoint)
                .distinct()
                .sorted()
                .collect(java.util.stream.Collectors.toList());
    }
    
    /**
     * Search routes with pagination
     */
    public List<RouteConfig> searchRoutes(int page, int size) {
        int offset = page * size;
        return routeConfigMapper.findWithPagination(offset, size);
    }
    
    /**
     * Get route configuration as map for quick lookup
     */
    public Map<String, RouteConfig> getRouteCacheSnapshot() {
        return new ConcurrentHashMap<>(routeCache);
    }
    
    /**
     * Bulk create/update routes
     */
    @CacheEvict(value = "routeConfig", allEntries = true)
    public int bulkCreateRoutes(List<RouteConfig> routes) {
        logger.info("Bulk creating {} routes", routes.size());
        
        int successCount = 0;
        for (RouteConfig route : routes) {
            try {
                route.setCreatedDate(LocalDateTime.now());
                route.setModifiedDate(LocalDateTime.now());
                
                int result = routeConfigMapper.insert(route);
                if (result > 0) {
                    successCount++;
                }
            } catch (Exception e) {
                logger.error("Failed to create route: branch={}, endpoint={}", 
                           route.getBranchCode(), route.getEndpoint(), e);
            }
        }
        
        refreshCache();
        logger.info("Successfully created {}/{} routes", successCount, routes.size());
        return successCount;
    }
    
    /**
     * Export all route configurations
     */
    public List<RouteConfig> exportAllRoutes() {
        return routeConfigMapper.findAll();
    }
    
    /**
     * Check if branch-endpoint combination already exists
     */
    public boolean routeExists(String branchCode, String endpoint) {
        RouteConfig existing = routeConfigMapper.findByBranchAndEndpoint(branchCode, endpoint);
        return existing != null;
    }
    
    /**
     * Get routes that need plugin updates
     */
    public List<RouteConfig> getRoutesForPlugin(String oldPluginId, String newPluginId) {
        List<RouteConfig> routes = routeConfigMapper.findByPluginId(oldPluginId);
        
        // Update plugin references
        for (RouteConfig route : routes) {
            route.setPluginId(newPluginId);
            route.setModifiedDate(LocalDateTime.now());
            routeConfigMapper.update(route);
        }
        
        refreshCache();
        return routes;
    }
}