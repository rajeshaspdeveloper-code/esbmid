package com.esb.middleware.mapper;

import com.esb.middleware.model.BranchServiceConfig;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface BranchServiceConfigMapper {
    
    /**
     * Find service configuration by branch and service ID
     */
    BranchServiceConfig findByBranchAndServiceId(@Param("branchCode") String branchCode, 
                                               @Param("serviceId") String serviceId);
    
    /**
     * Find all service configurations for a branch
     */
    List<BranchServiceConfig> findByBranch(@Param("branchCode") String branchCode);
    
    /**
     * Find all enabled service configurations
     */
    List<BranchServiceConfig> findAllEnabled();
    
    /**
     * Find all service configurations
     */
    List<BranchServiceConfig> findAll();
    
    /**
     * Find services by plugin ID
     */
    List<BranchServiceConfig> findByPluginId(@Param("pluginId") String pluginId);
    
    /**
     * Insert new service configuration
     */
    int insert(BranchServiceConfig serviceConfig);
    
    /**
     * Update existing service configuration
     */
    int update(BranchServiceConfig serviceConfig);
    
    /**
     * Delete service configuration by ID
     */
    int deleteById(@Param("id") Long id);
    
    /**
     * Enable/disable service configuration
     */
    int updateStatus(@Param("id") Long id, @Param("enabled") boolean enabled);
    
    /**
     * Find service by ID
     */
    BranchServiceConfig findById(@Param("id") Long id);
    
    /**
     * Count total services
     */
    int countTotal();
    
    /**
     * Count enabled services
     */
    int countEnabled();
    
    /**
     * Find services with pagination
     */
    List<BranchServiceConfig> findWithPagination(@Param("offset") int offset, 
                                                @Param("limit") int limit);
}