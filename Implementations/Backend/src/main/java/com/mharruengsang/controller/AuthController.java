package com.mharruengsang.controller;

import com.mharruengsang.dto.*;
import com.mharruengsang.service.AuthService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Authentication Controller
 * 
 * Handles user authentication, registration, and token management
 * 
 * BASE URL: /api/auth
 */
@RestController
@RequestMapping("/auth")
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:4000", "http://localhost:3000"})
@Slf4j
public class AuthController {
    
    private final AuthService authService;
    
    public AuthController(AuthService authService) {
        this.authService = authService;
    }
    
    /**
     * Register a new user
     * 
     * POST /api/auth/register
     * 
     * @param request RegisterRequest with user details
     * @return LoginResponse with JWT tokens
     */
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<LoginResponse>> register(@Valid @RequestBody RegisterRequest request) {
        try {
            log.info("Registration request received for email: {}", request.getEmail());
            LoginResponse response = authService.register(request);
            
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success(response, "User registered successfully"));
        } catch (IllegalArgumentException e) {
            log.error("Registration failed: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            log.error("Registration error", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Registration failed. Please try again."));
        }
    }
    
    /**
     * Login with email and password
     * 
     * POST /api/auth/login
     * 
     * @param request LoginRequest with credentials
     * @return LoginResponse (OTP will be sent, tokens provided after OTP verification)
     */
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@Valid @RequestBody LoginRequest request) {
        try {
            log.info("Login request received for email: {}", request.getEmail());
            LoginResponse response = authService.login(request);
            
            return ResponseEntity.ok(ApiResponse.success(response, "OTP sent successfully"));
        } catch (IllegalArgumentException e) {
            log.error("Login failed: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            log.error("Login error", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Login failed. Please try again."));
        }
    }
    
    /**
     * Verify OTP for two-factor authentication
     * 
     * POST /api/auth/otp
     * 
     * @param request OtpRequest with email and OTP code
     * @return LoginResponse with JWT tokens
     */
    @PostMapping("/otp")
    public ResponseEntity<ApiResponse<LoginResponse>> verifyOtp(@Valid @RequestBody OtpRequest request) {
        try {
            log.info("OTP verification request for email: {}", request.getEmail());
            LoginResponse response = authService.verifyOtp(request);
            
            return ResponseEntity.ok(ApiResponse.success(response, "Login successful"));
        } catch (IllegalArgumentException e) {
            log.error("OTP verification failed: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            log.error("OTP verification error", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("OTP verification failed. Please try again."));
        }
    }
    
    /**
     * Refresh access token
     * 
     * POST /api/auth/refresh
     * 
     * @param request Map containing refreshToken
     * @return LoginResponse with new access token
     */
    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<LoginResponse>> refresh(@RequestBody Map<String, String> request) {
        try {
            String refreshToken = request.get("refreshToken");
            if (refreshToken == null || refreshToken.isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(ApiResponse.error("Refresh token is required"));
            }
            
            LoginResponse response = authService.refreshToken(refreshToken);
            return ResponseEntity.ok(ApiResponse.success(response, "Token refreshed successfully"));
        } catch (IllegalArgumentException e) {
            log.error("Token refresh failed: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            log.error("Token refresh error", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Token refresh failed. Please try again."));
        }
    }
    
    /**
     * Logout user
     * 
     * POST /api/auth/logout
     * 
     * @param request Map containing token
     * @return Success message
     */
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<String>> logout(@RequestBody Map<String, String> request) {
        try {
            String token = request.get("token");
            if (token != null) {
                authService.logout(token);
            }
            return ResponseEntity.ok(ApiResponse.success("Logged out successfully", "Logout successful"));
        } catch (Exception e) {
            log.error("Logout error", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Logout failed. Please try again."));
        }
    }
    
    /**
     * Resend OTP
     * 
     * POST /api/auth/otp/resend
     * 
     * @param request Map containing email
     * @return Success message
     */
    @PostMapping("/otp/resend")
    public ResponseEntity<ApiResponse<String>> resendOtp(@RequestBody Map<String, String> request) {
        try {
            String email = request.get("email");
            if (email == null || email.isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(ApiResponse.error("Email is required"));
            }
            
            authService.sendOtp(email);
            return ResponseEntity.ok(ApiResponse.success("OTP sent successfully", "OTP sent to your phone"));
        } catch (Exception e) {
            log.error("Resend OTP error", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to send OTP. Please try again."));
        }
    }
    
    /**
     * Health check endpoint
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        Map<String, String> response = new HashMap<>();
        response.put("status", "UP");
        response.put("service", "auth-service");
        return ResponseEntity.ok(response);
    }
}
