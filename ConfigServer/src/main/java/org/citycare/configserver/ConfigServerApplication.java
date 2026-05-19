package org.citycare.configserver;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.config.server.EnableConfigServer;

/**
 * ConfigServer - Centralized configuration server for all CityCare microservices
 *
 * Spring Boot 3.2+ automatically enables Eureka client when
 * spring-cloud-starter-netflix-eureka-client dependency is present
 */
@SpringBootApplication
@EnableConfigServer
public class ConfigServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(ConfigServerApplication.class, args);
    }

}