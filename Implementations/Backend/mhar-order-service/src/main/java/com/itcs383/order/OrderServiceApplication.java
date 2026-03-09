package com.itcs383.order;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * Order Service Application
 * Handles order creation, tracking, and status management
 * 
 * Core responsibilities:
 * - Create new orders with validation
 * - Track order status (PENDING → PREPARING → PICKED_UP → DELIVERED)
 * - Manage order items and pricing
 * - Integrate with Restaurant and Payment services
 */
@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients
@EnableJpaAuditing
public class OrderServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(OrderServiceApplication.class, args);
    }
}
