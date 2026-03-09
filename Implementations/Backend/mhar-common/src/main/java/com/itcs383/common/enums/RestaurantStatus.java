package com.itcs383.common.enums;

/**
 * Restaurant Status Enum
 * 
 * Represents the approval and operational status of restaurants
 */
public enum RestaurantStatus {
    /**
     * Restaurant registration is pending admin approval
     */
    PENDING,
    
    /**
     * Restaurant has been approved and can operate
     */
    APPROVED,
    
    /**
     * Restaurant is temporarily suspended
     */
    SUSPENDED,
    
    /**
     * Restaurant has been rejected or permanently banned
     */
    REJECTED
}
