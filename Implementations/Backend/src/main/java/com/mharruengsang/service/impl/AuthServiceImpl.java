package com.mharruengsang.service.impl;

import com.mharruengsang.dto.LoginRequest;
import com.mharruengsang.dto.LoginResponse;
import com.mharruengsang.dto.OtpRequest;
import com.mharruengsang.dto.RegisterRequest;
import com.mharruengsang.entity.User;
import com.mharruengsang.repository.UserRepository;
import com.mharruengsang.service.AuthService;
import com.mharruengsang.util.JwtUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Random;

@Service
@Slf4j
@Transactional
public class AuthServiceImpl implements AuthService {
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    
    // In-memory OTP storage (use Redis in production)
    private final Map<String, String> otpStorage = new HashMap<>();
    
    public AuthServiceImpl(UserRepository userRepository, 
                          PasswordEncoder passwordEncoder,
                          JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }
    
    @Override
    public LoginResponse register(RegisterRequest request) {
        log.info("Registering new user with email: {}", request.getEmail());
        
        // Check if user already exists
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new IllegalArgumentException("Email already registered");
        }
        
        if (userRepository.findByPhoneNumber(request.getPhoneNumber()).isPresent()) {
            throw new IllegalArgumentException("Phone number already registered");
        }
        
        // Create new user
        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .phoneNumber(request.getPhoneNumber())
                .role(User.UserRole.valueOf(request.getRole()))
                .address(request.getAddress())
                .latitude(request.getLatitude())
                .longitude(request.getLongitude())
                .enabled(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        
        user = userRepository.save(user);
        log.info("User registered successfully: {}", user.getEmail());
        
        // Generate tokens
        String token = jwtUtil.generateToken(user.getEmail(), user.getRole().name(), user.getId());
        String refreshToken = jwtUtil.generateRefreshToken(user.getEmail());
        
        return LoginResponse.builder()
                .token(token)
                .refreshToken(refreshToken)
                .user(convertToUserDTO(user))
                .message("Registration successful")
                .build();
    }
    
    @Override
    public LoginResponse login(LoginRequest request) {
        log.info("Login attempt for email: {}", request.getEmail());
        
        // Find user by email
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("Invalid email or password"));
        
        // Verify password
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            log.warn("Failed login attempt for email: {}", request.getEmail());
            throw new IllegalArgumentException("Invalid email or password");
        }
        
        if (!user.getEnabled()) {
            throw new IllegalArgumentException("Account is disabled");
        }
        
        // Generate OTP and send to user's phone
        sendOtp(user.getEmail());
        
        log.info("Login credentials verified for: {}. OTP sent.", user.getEmail());
        
        // Return response with OTP required flag
        return LoginResponse.builder()
                .user(convertToUserDTO(user))
                .message("OTP sent to your phone. Please verify to complete login.")
                .build();
    }
    
    @Override
    public LoginResponse verifyOtp(OtpRequest request) {
        log.info("Verifying OTP for email: {}", request.getEmail());
        
        // Verify OTP
        String storedOtp = otpStorage.get(request.getEmail());
        if (storedOtp == null || !storedOtp.equals(request.getOtp())) {
            throw new IllegalArgumentException("Invalid or expired OTP");
        }
        
        // Remove OTP after successful verification
        otpStorage.remove(request.getEmail());
        
        // Get user
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        
        // Generate tokens
        String token = jwtUtil.generateToken(user.getEmail(), user.getRole().name(), user.getId());
        String refreshToken = jwtUtil.generateRefreshToken(user.getEmail());
        
        log.info("OTP verified successfully for: {}", user.getEmail());
        
        return LoginResponse.builder()
                .token(token)
                .refreshToken(refreshToken)
                .user(convertToUserDTO(user))
                .message("Login successful")
                .build();
    }
    
    @Override
    public void sendOtp(String email) {
        // Generate 6-digit OTP
        String otp = String.format("%06d", new Random().nextInt(999999));
        
        // Store OTP (in production, use Redis with expiration)
        otpStorage.put(email, otp);
        
        // In production, send OTP via SMS (Twilio, etc.)
        log.info("OTP generated for {}: {} (This would be sent via SMS in production)", email, otp);
        
        // For demo purposes, log the OTP
        System.out.println("===========================================");
        System.out.println("OTP for " + email + ": " + otp);
        System.out.println("===========================================");
    }
    
    @Override
    public LoginResponse refreshToken(String refreshToken) {
        try {
            String username = jwtUtil.extractUsername(refreshToken);
            
            if (jwtUtil.validateToken(refreshToken, username)) {
                User user = userRepository.findByEmail(username)
                        .orElseThrow(() -> new IllegalArgumentException("User not found"));
                
                String newToken = jwtUtil.generateToken(user.getEmail(), user.getRole().name(), user.getId());
                
                return LoginResponse.builder()
                        .token(newToken)
                        .refreshToken(refreshToken)
                        .user(convertToUserDTO(user))
                        .message("Token refreshed successfully")
                        .build();
            }
        } catch (Exception e) {
            log.error("Error refreshing token", e);
        }
        
        throw new IllegalArgumentException("Invalid refresh token");
    }
    
    @Override
    public void logout(String token) {
        // In production, add token to blacklist in Redis
        log.info("User logged out");
    }
    
    private LoginResponse.UserDTO convertToUserDTO(User user) {
        return LoginResponse.UserDTO.builder()
                .id(user.getId())
                .email(user.getEmail())
                .name(user.getName())
                .role(user.getRole().name())
                .phoneNumber(user.getPhoneNumber())
                .build();
    }
}
