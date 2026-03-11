package com.itcs383.restaurant;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.openfeign.EnableFeignClients;

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
@EnableCaching
@EnableFeignClients
public class RestaurantServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(RestaurantServiceApplication.class, args);
    }
}
