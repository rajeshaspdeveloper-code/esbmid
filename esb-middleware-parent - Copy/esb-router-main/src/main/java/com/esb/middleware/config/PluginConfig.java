package com.esb.middleware.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.io.File;
import java.util.concurrent.Executor;

/**
 * Plugin-related configuration
 */
@Configuration
public class PluginConfig {

    @Value("${esb.plugin.directory:plugins}")
    private String pluginDirectory;

    @Value("${esb.plugin.reload.enabled:true}")
    private boolean reloadEnabled;

    @Value("${esb.plugin.reload.interval:300000}")
    private long reloadInterval;

    @Value("${esb.plugin.thread.core-pool-size:5}")
    private int corePoolSize;

    @Value("${esb.plugin.thread.max-pool-size:20}")
    private int maxPoolSize;

    @Value("${esb.plugin.thread.queue-capacity:100}")
    private int queueCapacity;

    @Bean
    public File pluginDirectory() {
        File dir = new File(pluginDirectory);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        return dir;
    }

    @Bean(name = "pluginExecutor")
    public Executor pluginExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(corePoolSize);
        executor.setMaxPoolSize(maxPoolSize);
        executor.setQueueCapacity(queueCapacity);
        executor.setThreadNamePrefix("Plugin-");
        executor.setRejectedExecutionHandler(new java.util.concurrent.ThreadPoolExecutor.CallerRunsPolicy());
        executor.initialize();
        return executor;
    }

    public String getPluginDirectory() {
        return pluginDirectory;
    }

    public boolean isReloadEnabled() {
        return reloadEnabled;
    }

    public long getReloadInterval() {
        return reloadInterval;
    }
}