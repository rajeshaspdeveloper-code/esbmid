package com.esb.middleware.service;

import com.esb.middleware.mapper.BranchServiceConfigMapper;
import com.esb.middleware.model.BranchServiceConfig;
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

@Service
public class BranchServiceConfigService {
    
    private static final Logger logger = LoggerFactory.getLogger(BranchServiceConfigService.class);
    
    @Autowired
    private BranchServiceConfigMapper branchServiceConfigMapper;
    
    // In-memory cache for fast service lookups
    private final Map<String, BranchServiceConfig> serviceCache = new ConcurrentHashMap<>();
    
    @PostConstruct
    public void initializeCache() {
        logger.info("Initializing branch service configuration cache");
        refreshCache();
        logger.info("Branch service configuration cache initialized with {} services", serviceCache.size());
    }
    
    /**
     * Get service configuration for branch and service ID
     */
    @Cacheable(value = "branchServiceConfig", key = "#branchCode + '_' + #serviceId")
    public BranchServiceConfig getServiceConfig(String branchCode, String serviceId) {
        String cacheKey = branchCode + "_" + serviceId;
        BranchServiceConfig config = serviceCache.get(cacheKey);
        
        if (config == null) {
            // Fetch from database if not in cache
            config = branchServiceConfigMapper.findByBranchAndServiceId(branchCode, serviceId);
            if (config != null && config.isEnabled()) {
                serviceCache.put(cacheKey, config);
            }
        }
        
        return config != null && config.isEnabled() ? config : null;
    }
    
    /**
     * Get all service configurations for a branch
     */
    public List<BranchServiceConfig> getServicesByBranch(String branchCode) {
        return branchServiceConfigMapper.findByBranch(branchCode);
    }
    
    /**
     * Get all enabled service configurations
     */
    public List<BranchServiceConfig> getAllEnabledServices() {
        return branchServiceConfigMapper.findAllEnabled();
    }
    
    /**
     * Create new service configuration
     */
    @CacheEvict(value = "branchServiceConfig", allEntries = true)
    public BranchServiceConfig createService(BranchServiceConfig serviceConfig) {
        logger.info("Creating new service config: branch={}, serviceId={}, pluginId={}", 
                   serviceConfig.getBranchCode(), serviceConfig.getServiceId(), serviceConfig.getPluginId());
        
        serviceConfig.setCreatedDate(LocalDateTime.now());
        serviceConfig.setModifiedDate(LocalDateTime.now());
        
        int result = branchServiceConfigMapper.insert(serviceConfig);
        if (result > 0) {
            refreshCache();
            logger.info("Successfully created service config with ID: {}", serviceConfig.getId());
            return serviceConfig;
        } else {
            throw new RuntimeException("Failed to create service configuration");
        }
    }
    
    /**
     * Update existing service configuration
     */
    @CacheEvict(value = "branchServiceConfig", allEntries = true)
    public BranchServiceConfig updateService(BranchServiceConfig serviceConfig) {
        logger.info("Updating service config: ID={}, branch={}, serviceId={}", 
                   serviceConfig.getId(), serviceConfig.getBranchCode(), serviceConfig.getServiceId());
        
        serviceConfig.setModifiedDate(LocalDateTime.now());
        
        int result = branchServiceConfigMapper.update(serviceConfig);
        if (result > 0) {
            refreshCache();
            logger.info("Successfully updated service config with ID: {}", serviceConfig.getId());
            return serviceConfig;
        } else {
            throw new RuntimeException("Failed to update service configuration");
        }
    }
    
    /**
     * Delete service configuration
     */
    @CacheEvict(value = "branchServiceConfig", allEntries = true)
    public boolean deleteService(Long serviceId) {
        logger.info("Deleting service config with ID: {}", serviceId);
        
        int result = branchServiceConfigMapper.deleteById(serviceId);
        if (result > 0) {
            refreshCache();
            logger.info("Successfully deleted service config with ID: {}", serviceId);
            return true;
        } else {
            logger.warn("Service config not found for deletion: {}", serviceId);
            return false;
        }
    }
    
    /**
     * Enable/disable service configuration
     */
    @CacheEvict(value = "branchServiceConfig", allEntries = true)
    public boolean toggleService(Long serviceId, boolean enabled) {
        logger.info("Toggling service config: ID={}, enabled={}", serviceId, enabled);
        
        int result = branchServiceConfigMapper.updateStatus(serviceId, enabled);
        if (result > 0) {
            refreshCache();
            logger.info("Successfully toggled service config: ID={}, enabled={}", serviceId, enabled);
            return true;
        } else {
            logger.warn("Service config not found for toggle: {}", serviceId);
            return false;
        }
    }
    
    /**
     * Get service configuration by ID
     */
    public BranchServiceConfig getServiceById(Long serviceId) {
        return branchServiceConfigMapper.findById(serviceId);
    }
    
    /**
     * Get services by plugin ID
     */
    public List<BranchServiceConfig> getServicesByPlugin(String pluginId) {
        return branchServiceConfigMapper.findByPluginId(pluginId);
    }
    
    /**
     * Refresh the entire service configuration cache
     */
    @CacheEvict(value = "branchServiceConfig", allEntries = true)
    public void refreshCache() {
        logger.info("Refreshing branch service configuration cache");
        
        serviceCache.clear();
        List<BranchServiceConfig> allServices = branchServiceConfigMapper.findAllEnabled();
        
        for (BranchServiceConfig service : allServices) {
            String cacheKey = service.getBranchCode() + "_" + service.getServiceId();
            serviceCache.put(cacheKey, service);
        }
        
        logger.info("Branch service configuration cache refreshed with {} services", serviceCache.size());
    }
    
    /**
     * Get configuration statistics
     */
    public Map<String, Object> getConfigurationStats() {
        Map<String, Object> stats = new ConcurrentHashMap<>();
        stats.put("totalServices", branchServiceConfigMapper.countTotal());
        stats.put("enabledServices", branchServiceConfigMapper.countEnabled());
        stats.put("cachedServices", serviceCache.size());
        return stats;
    }
}