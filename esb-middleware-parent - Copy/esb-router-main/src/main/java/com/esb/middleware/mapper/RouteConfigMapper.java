package com.esb.middleware.mapper;

import com.esb.middleware.model.RouteConfig;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * MyBatis mapper for RouteConfig operations
 */
@Mapper
public interface RouteConfigMapper {
    
    /**
     * Find route configuration by branch and endpoint
     */
    RouteConfig findByBranchAndEndpoint(@Param("branchCode") String branchCode, 
                                      @Param("endpoint") String endpoint);
    
    /**
     * Find all route configurations for a branch
     */
    List<RouteConfig> findByBranch(@Param("branchCode") String branchCode);
    
    /**
     * Find all enabled route configurations
     */
    List<RouteConfig> findAllEnabled();
    
    /**
     * Find all route configurations
     */
    List<RouteConfig> findAll();
    
    /**
     * Find routes by plugin ID
     */
    List<RouteConfig> findByPluginId(@Param("pluginId") String pluginId);
    
    /**
     * Insert new route configuration
     */
    int insert(RouteConfig routeConfig);
    
    /**
     * Update existing route configuration
     */
    int update(RouteConfig routeConfig);
    
    /**
     * Delete route configuration by ID
     */
    int deleteById(@Param("id") Long id);
    
    /**
     * Enable/disable route configuration
     */
    int updateStatus(@Param("id") Long id, @Param("enabled") boolean enabled);
    
    /**
     * Find route by ID
     */
    RouteConfig findById(@Param("id") Long id);
    
    /**
     * Count total routes
     */
    int countTotal();
    
    /**
     * Count enabled routes
     */
    int countEnabled();
    
    /**
     * Find routes with pagination
     */
    List<RouteConfig> findWithPagination(@Param("offset") int offset, 
                                        @Param("limit") int limit);
}