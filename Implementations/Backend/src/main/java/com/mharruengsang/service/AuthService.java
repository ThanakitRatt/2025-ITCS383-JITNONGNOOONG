package com.mharruengsang.service;

import com.mharruengsang.dto.LoginRequest;
import com.mharruengsang.dto.LoginResponse;
import com.mharruengsang.dto.OtpRequest;
import com.mharruengsang.dto.RegisterRequest;

public interface AuthService {
    
    /**
     * Register a new user
     */
    LoginResponse register(RegisterRequest request);
    
    /**
     * Authenticate user with email and password
     */
    LoginResponse login(LoginRequest request);
    
    /**
     * Verify OTP for two-factor authentication
     */
    LoginResponse verifyOtp(OtpRequest request);
    
    /**
     * Send OTP to user's phone
     */
    void sendOtp(String email);
    
    /**
     * Refresh access token using refresh token
     */
    LoginResponse refreshToken(String refreshToken);
    
    /**
     * Logout user (invalidate tokens)
     */
    void logout(String token);
}
