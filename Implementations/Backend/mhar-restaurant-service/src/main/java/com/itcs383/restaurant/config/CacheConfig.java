package com.itcs383.restaurant.config;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

/**
 * Redis Cache Configuration
 * 
 * Configures Redis caching for the Restaurant Service with
 * different TTL settings for different cache types
 */
@Configuration
@EnableCaching
public class CacheConfig {

    @Value("${app.restaurant.restaurant-cache-ttl:600}")
    private long restaurantCacheTtl;

    @Value("${app.restaurant.menu-cache-ttl:300}")
    private long menuCacheTtl;

    @Value("${app.restaurant.category-cache-ttl:1800}")
    private long categoryCacheTtl;

    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        
        // Configure serializers
        template.setKeySerializer(new StringRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(genericJackson2JsonRedisSerializer());
        template.setHashValueSerializer(genericJackson2JsonRedisSerializer());
        
        return template;
    }

    @Bean
    public CacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        // Create ObjectMapper with proper configuration
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.activateDefaultTyping(
            LaissezFaireSubTypeValidator.instance,
            ObjectMapper.DefaultTyping.NON_FINAL,
            JsonTypeInfo.As.PROPERTY
        );

        GenericJackson2JsonRedisSerializer serializer = new GenericJackson2JsonRedisSerializer(objectMapper);

        // Default cache configuration
        RedisCacheConfiguration defaultCacheConfig = RedisCacheConfiguration.defaultCacheConfig()
            .entryTtl(Duration.ofMinutes(10))
            .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
            .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(serializer));

        // Specific cache configurations with different TTLs
        Map<String, RedisCacheConfiguration> cacheConfigurations = new HashMap<>();
        
        // Restaurant data cache (10 minutes)
        cacheConfigurations.put("restaurants", defaultCacheConfig.entryTtl(Duration.ofSeconds(restaurantCacheTtl)));
        cacheConfigurations.put("restaurant-list", defaultCacheConfig.entryTtl(Duration.ofSeconds(restaurantCacheTtl)));
        cacheConfigurations.put("restaurants-by-cuisine", defaultCacheConfig.entryTtl(Duration.ofSeconds(restaurantCacheTtl)));
        cacheConfigurations.put("top-restaurants", defaultCacheConfig.entryTtl(Duration.ofMinutes(15)));
        
        // Menu data cache (5 minutes)
        cacheConfigurations.put("restaurant-menu", defaultCacheConfig.entryTtl(Duration.ofSeconds(menuCacheTtl)));
        cacheConfigurations.put("menu-items", defaultCacheConfig.entryTtl(Duration.ofSeconds(menuCacheTtl)));
        cacheConfigurations.put("featured-items", defaultCacheConfig.entryTtl(Duration.ofSeconds(menuCacheTtl)));
        
        // Category data cache (30 minutes)
        cacheConfigurations.put("menu-categories", defaultCacheConfig.entryTtl(Duration.ofSeconds(categoryCacheTtl)));
        
        // Static data cache (1 hour)
        cacheConfigurations.put("cuisine-types", defaultCacheConfig.entryTtl(Duration.ofHours(1)));

        return RedisCacheManager.builder(connectionFactory)
            .cacheDefaults(defaultCacheConfig)
            .withInitialCacheConfigurations(cacheConfigurations)
            .build();
    }

    @Bean
    public GenericJackson2JsonRedisSerializer genericJackson2JsonRedisSerializer() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.activateDefaultTyping(
            LaissezFaireSubTypeValidator.instance,
            ObjectMapper.DefaultTyping.NON_FINAL,
            JsonTypeInfo.As.PROPERTY
        );
        return new GenericJackson2JsonRedisSerializer(objectMapper);
    }
}
