package com.itcs383.restaurant.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * Service for managing users in mhar_auth database
 */
@Service
public class UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);
    
    private final JdbcTemplate authJdbcTemplate;

    public UserService(@Qualifier("authJdbcTemplate") JdbcTemplate authJdbcTemplate) {
        this.authJdbcTemplate = authJdbcTemplate;
    }

    /**
     * Check if email already exists
     */
    public boolean emailExists(String email) {
        String sql = "SELECT COUNT(*) FROM users WHERE email = ?";
        Integer count = authJdbcTemplate.queryForObject(sql, Integer.class, email);
        return count != null && count > 0;
    }

    /**
     * Check if phone number already exists
     */
    public boolean phoneExists(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.trim().isEmpty()) {
            return false;
        }
        String sql = "SELECT COUNT(*) FROM users WHERE phone_number = ?";
        Integer count = authJdbcTemplate.queryForObject(sql, Integer.class, phoneNumber);
        return count != null && count > 0;
    }

    /**
     * Create a new user in the database
     * 
     * @param userData Map containing: name, email, password (hashed), phoneNumber, role, address, latitude, longitude
     * @return Created user ID
     */
    public Long createUser(Map<String, Object> userData) {
        String sql = "INSERT INTO users (name, email, password, phone_number, role, address, latitude, longitude, balance, enabled, created_at, updated_at) " +
                     "VALUES (?, ?, ?, ?, ?::varchar, ?, ?, ?, 0.00, true, ?, ?) RETURNING id";
        
        LocalDateTime now = LocalDateTime.now();
        
        try {
            Long userId = authJdbcTemplate.queryForObject(sql, Long.class,
                userData.get("name"),
                userData.get("email"),
                userData.get("password"),
                userData.get("phoneNumber"),
                userData.get("role"),
                userData.get("address"),
                userData.get("latitude"),
                userData.get("longitude"),
                Timestamp.valueOf(now),
                Timestamp.valueOf(now)
            );
            
            logger.info("User created successfully with ID: {}", userId);
            return userId;
            
        } catch (Exception e) {
            logger.error("Error creating user: {}", e.getMessage());
            throw new IllegalStateException("Failed to create user: " + e.getMessage(), e);
        }
    }

    /**
     * Get user by email for authentication
     */
    public Map<String, Object> getUserByEmail(String email) {
        String sql = "SELECT id, name, email, password, phone_number, role, address, latitude, longitude, balance FROM users WHERE email = ?";
        
        try {
            return authJdbcTemplate.queryForMap(sql, email);
        } catch (Exception e) {
            logger.debug("User not found with email: {}", email);
            return null;
        }
    }

    /**
     * Validate user credentials
     */
    public Map<String, Object> validateCredentials(String email, String hashedPassword) {
        String sql = "SELECT id, name, email, phone_number, role, balance FROM users WHERE email = ? AND password = ? AND enabled = true";
        
        try {
            return authJdbcTemplate.queryForMap(sql, email, hashedPassword);
        } catch (Exception e) {
            logger.debug("Invalid credentials for email: {}", email);
            return null;
        }
    }
}
