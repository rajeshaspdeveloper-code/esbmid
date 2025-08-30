package com.esb.middleware.mapper;

import com.esb.middleware.model.PluginConfigModel;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

/**
 * MyBatis mapper for PluginConfig operations
 */
@Mapper
public interface PluginConfigMapper {
    
    /**
     * Find plugin configuration by plugin ID
     */
    PluginConfigModel findByPluginId(@Param("pluginId") String pluginId);
    
    /**
     * Find all plugin configurations
     */
    List<PluginConfigModel> findAll();
    
    /**
     * Find all enabled plugin configurations
     */
    List<PluginConfigModel> findAllEnabled();
    
    /**
     * Find plugin by JAR file n
     */
    PluginConfigModel findByJarFileName(@Param("jarFileName") String jarFileName);
    
    /**
     * Insert new plugin configuration
     */
    int insert(PluginConfigModel pluginConfig);
    
    /**
     * Update existing plugin configuration
     */
    int update(PluginConfigModel pluginConfig);
    
    /**
     * Delete plugin configuration by ID
     */
    int deleteById(@Param("id") Long id);
    
    /**
     * Delete plugin configuration by plugin ID
     */
    int deleteByPluginId(@Param("pluginId") String pluginId);
    
    /**
     * Enable/disable plugin
     */
    int updateStatus(@Param("pluginId") String pluginId, 
                    @Param("enabled") boolean enabled, 
                    @Param("status") String status);
    
    /**
     * Update plugin loaded time
     */
    int updateLoadedTime(@Param("pluginId") String pluginId, 
                        @Param("loadedTime") LocalDateTime loadedTime);
    
    /**
     * Find plugin by ID
     */
    PluginConfigModel findById(@Param("id") Long id);
    
    /**
     * Count total plugins
     */
    int countTotal();
    
    /**
     * Count enabled plugins
     */
    int countEnabled();
    
    /**
     * Find plugins by status
     */
    List<PluginConfigModel> findByStatus(@Param("status") String status);
    
    /**
     * Check if plugin exists
     */
    boolean existsByPluginId(@Param("pluginId") String pluginId);
}