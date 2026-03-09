package com.itcs383.restaurant;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * Restaurant Service Application
 * 
 * Handles restaurant information, menu management, and restaurant operations
 * 
 * Features:
 * - Restaurant CRUD operations
 * - Menu item management
 * - Category management
 * - Restaurant search and filtering
 * - Redis caching for performance
 * - Service discovery integration
 */
@SpringBootApplication
@EnableJpaAuditing
@EnableCaching
@EnableFeignClients
public class RestaurantServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(RestaurantServiceApplication.class, args);
    }
}
