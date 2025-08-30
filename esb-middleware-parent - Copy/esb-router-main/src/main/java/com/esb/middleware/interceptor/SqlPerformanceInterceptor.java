package com.esb.middleware.interceptor;

import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.plugin.*;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;

/**
 * MyBatis SQL Performance Interceptor
 */
@Intercepts({
    @Signature(type = Executor.class, method = "query", args = {MappedStatement.class, Object.class, RowBounds.class, ResultHandler.class}),
    @Signature(type = Executor.class, method = "update", args = {MappedStatement.class, Object.class})
})
public class SqlPerformanceInterceptor implements Interceptor {
    
    private static final Logger logger = LoggerFactory.getLogger(SqlPerformanceInterceptor.class);
    
    private long maxTime = 5000; // Default 5 seconds
    private boolean format = true;
    
    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        long startTime = System.currentTimeMillis();
        
        try {
            return invocation.proceed();
        } finally {
            long endTime = System.currentTimeMillis();
            long executionTime = endTime - startTime;
            
            if (executionTime >= maxTime) {
                MappedStatement mappedStatement = (MappedStatement) invocation.getArgs()[0];
                logger.warn("Slow SQL detected - ID: {}, Execution time: {}ms", 
                           mappedStatement.getId(), executionTime);
            } else if (logger.isDebugEnabled()) {
                MappedStatement mappedStatement = (MappedStatement) invocation.getArgs()[0];
                logger.debug("SQL executed - ID: {}, Execution time: {}ms", 
                           mappedStatement.getId(), executionTime);
            }
        }
    }
    
    @Override
    public Object plugin(Object target) {
        return Plugin.wrap(target, this);
    }
    
    @Override
    public void setProperties(Properties properties) {
        String maxTimeStr = properties.getProperty("maxTime");
        if (maxTimeStr != null) {
            this.maxTime = Long.parseLong(maxTimeStr);
        }
        
        String formatStr = properties.getProperty("format");
        if (formatStr != null) {
            this.format = Boolean.parseBoolean(formatStr);
        }
    }
}