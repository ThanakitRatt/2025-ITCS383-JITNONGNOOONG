package com.itcs383.restaurant.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * JPA Configuration
 * Enables JPA auditing for automatic timestamp management
 */
@Configuration
@EnableJpaAuditing
public class JpaConfig {
}
