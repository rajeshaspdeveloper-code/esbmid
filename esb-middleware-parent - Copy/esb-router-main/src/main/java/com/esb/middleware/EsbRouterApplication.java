package com.esb.middleware;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Main Spring Boot Apfplication for ESB Router
 */
@SpringBootApplication
@MapperScan("com.esb.middleware.mapper")
@EnableAsync
@EnableScheduling
public class EsbRouterApplication extends SpringBootServletInitializer {

    public static void main(String[] args) {

        System.setProperty("spring.profiles.default", "dev");
        System.out.println("SPRING DATASOURCE URL = " + System.getProperty("spring.datasource.url"));

        SpringApplication.run(EsbRouterApplication.class, args);
    }
}