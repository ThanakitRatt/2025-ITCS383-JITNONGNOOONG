package com.itcs383.order.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

/**
 * Service to handle rider payments and balance updates
 * Updates user balance in mhar_auth database when orders are delivered
 */
@Service
public class RiderPaymentService {

    private static final Logger logger = LoggerFactory.getLogger(RiderPaymentService.class);
    
    private static final String DB_URL = "jdbc:postgresql://localhost:5432/mhar_auth";
    private static final String DB_USER = System.getenv().getOrDefault("DB_USERNAME", "mhar_user");
    private static final String DB_PASSWORD = System.getenv().getOrDefault("DB_PASSWORD", "mhar_password");

    /**
     * Credit rider with delivery fee when order is completed
     * Updates the balance column in users table (mhar_auth database)
     * 
     * @param riderId User ID of the rider
     * @param amount Delivery fee to credit
     * @return true if update was successful, false otherwise
     */
    @Transactional
    public boolean creditRiderEarnings(Long riderId, BigDecimal amount) {
        if (riderId == null || amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            logger.warn("Invalid rider payment request: riderId={}, amount={}", riderId, amount);
            return false;
        }

        String sql = "UPDATE users SET balance = balance + ?, updated_at = NOW() WHERE id = ? AND role = 'RIDER'";
        
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setBigDecimal(1, amount);
            pstmt.setLong(2, riderId);
            
            int rowsAffected = pstmt.executeUpdate();

            if (rowsAffected > 0) {
                logger.info("Credited rider {} with amount {}", riderId, amount);
                return true;
            } else {
                logger.warn("Failed to credit rider {}: user not found or not a RIDER", riderId);
                return false;
            }
        } catch (Exception e) {
            logger.error("Error crediting rider {} with amount {}: {}", riderId, amount, e.getMessage());
            return false;
        }
    }

    /**
     * Get rider's current balance
     * 
     * @param riderId User ID of the rider
     * @return Current balance or null if rider not found
     */
    public BigDecimal getRiderBalance(Long riderId) {
        String sql = "SELECT balance FROM users WHERE id = ? AND role = 'RIDER'";
        
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setLong(1, riderId);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getBigDecimal("balance");
                }
            }
        } catch (Exception e) {
            logger.error("Error fetching balance for rider {}: {}", riderId, e.getMessage());
        }
        
        return null;
    }
}
