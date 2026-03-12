package com.itcs383.restaurant.controller;

import com.itcs383.common.dto.ApiResponse;
import com.itcs383.restaurant.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Authentication endpoint served by restaurant-service.
 * Handles user registration, login, and OTP verification.
 *
 * Gateway routes /api/v1/auth/** → this service at /api/auth/**
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);
    private static final String ROLE_CUSTOMER = "CUSTOMER";
    private static final String KEY_EMAIL     = "email";
    private static final String KEY_PASSWORD  = "password";
    
    private final UserService userService;
    private final BCryptPasswordEncoder passwordEncoder;

    @Autowired
    public AuthController(UserService userService) {
        this.userService = userService;
        this.passwordEncoder = new BCryptPasswordEncoder();
    }

    // email → [plainPassword, role, id (Long), name]
    private static final Map<String, Object[]> DEMO_USERS = Map.of(
        "customer@foodexpress.com",    new Object[]{"Customer123!",    ROLE_CUSTOMER,   100L, "Demo Customer"},
        "sarah@foodexpress.com",       new Object[]{"Sarah123!",       ROLE_CUSTOMER,   101L, "Sarah Wilson"},
        "restaurant@foodexpress.com",  new Object[]{"Restaurant123!",  "RESTAURANT",   2L, "Bangkok Street Food"},
        "sushi@foodexpress.com",       new Object[]{"Sushi123!",       "RESTAURANT",   6L, "Sushi Master"},
        "rider@foodexpress.com",       new Object[]{"Rider123!",       "RIDER",      200L, "Demo Rider"},
        "admin@foodexpress.com",       new Object[]{"Admin123!",       "ADMIN",      300L, "Admin User"}
    );

    // POST /api/auth/login
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<Map<String, Object>>> login(
            @RequestBody Map<String, String> req) {

        String email    = req.getOrDefault(KEY_EMAIL, "");
        String password = req.getOrDefault(KEY_PASSWORD, "");

        Object[] demo = DEMO_USERS.get(email);
        if (demo == null || !demo[0].equals(password)) {
            return ResponseEntity.status(401)
                    .body(ApiResponse.error("Invalid " + KEY_EMAIL + " or password"));
        }

        Map<String, Object> data = buildResponse(email, demo, "OTP sent to your email");
        return ResponseEntity.ok(ApiResponse.success(data, "OTP sent successfully"));
    }

    // POST /api/auth/otp  — accepts any 6-digit OTP for demo
    @PostMapping("/otp")
    public ResponseEntity<ApiResponse<Map<String, Object>>> verifyOtp(
            @RequestBody Map<String, String> req) {

        String email = req.getOrDefault(KEY_EMAIL, "");
        Object[] demo = DEMO_USERS.get(email);

        if (demo == null) {
            return ResponseEntity.status(401)
                    .body(ApiResponse.error("User not found"));
        }

        Map<String, Object> data = buildResponse(email, demo, "Login successful");
        return ResponseEntity.ok(ApiResponse.success(data, "Login successful"));
    }

    // POST /api/auth/register - Real database registration
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<Map<String, Object>>> register(
            @RequestBody Map<String, Object> req) {

        try {
            String email = (String) req.getOrDefault(KEY_EMAIL, "");
            String password = (String) req.getOrDefault(KEY_PASSWORD, "");
            String name = (String) req.getOrDefault("name", "New User");
            String phoneNumber = (String) req.getOrDefault("phoneNumber", "");
            String role = (String) req.getOrDefault("role", ROLE_CUSTOMER);
            
            // Validation
            if (email.isEmpty()) {
                return ResponseEntity.status(400)
                        .body(ApiResponse.error("Email is required"));
            }
            
            // Check if email already exists
            if (userService.emailExists(email)) {
                logger.warn("Registration attempt with existing email: {}", email);
                return ResponseEntity.status(400)
                        .body(ApiResponse.error("Email already registered"));
            }
            
            // Check if phone number already exists
            if (!phoneNumber.isEmpty() && userService.phoneExists(phoneNumber)) {
                logger.warn("Registration attempt with existing phone: {}", phoneNumber);
                return ResponseEntity.status(400)
                        .body(ApiResponse.error("Phone number already registered"));
            }
            
            // Hash password (generate random UUID if not provided, for OTP-based registration)
            String effectivePassword = password.isEmpty() ? java.util.UUID.randomUUID().toString() : password;
            String hashedPassword = passwordEncoder.encode(effectivePassword);
            
            // Prepare user data
            Map<String, Object> userData = new HashMap<>();
            userData.put("name", name);
            userData.put(KEY_EMAIL, email);
            userData.put(KEY_PASSWORD, hashedPassword);
            userData.put("phoneNumber", phoneNumber.isEmpty() ? null : phoneNumber);
            userData.put("role", role.toUpperCase());
            userData.put("address", req.get("address"));
            userData.put("latitude", req.get("latitude"));
            userData.put("longitude", req.get("longitude"));
            
            // Create user in database
            Long userId = userService.createUser(userData);
            
            logger.info("User registered successfully with ID: {} and email: {}", userId, email);
            
            // Build response
            Map<String, Object> userMap = new HashMap<>();
            userMap.put("id", userId);
            userMap.put(KEY_EMAIL, email);
            userMap.put("name", name);
            userMap.put("role", role.toUpperCase());
            
            Map<String, Object> data = new HashMap<>();
            data.put("token", "jwt-" + userId);
            data.put("refreshToken", "refresh-" + userId);
            data.put("user", userMap);
            data.put("message", "Registration successful");
            
            return ResponseEntity.status(201)
                    .body(ApiResponse.success(data, "User registered successfully"));
                    
        } catch (Exception e) {
            logger.error("Registration error: {}", e.getMessage(), e);
            return ResponseEntity.status(500)
                    .body(ApiResponse.error("Registration failed: " + e.getMessage()));
        }
    }

    // GET /api/auth/me
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<Map<String, String>>> me() {
        return ResponseEntity.ok(ApiResponse.success(Map.of("status", "ok")));
    }

    // ── helpers ──────────────────────────────────────────────────────────────

    private Map<String, Object> buildResponse(String email, Object[] demo, String message) {
        Map<String, Object> userMap = new HashMap<>();
        userMap.put("id",    demo[2]);
        userMap.put(KEY_EMAIL, email);
        userMap.put("role",  demo[1]);
        userMap.put("name",  demo[3]);

        long ts = System.currentTimeMillis();
        Map<String, Object> data = new HashMap<>();
        data.put("token",        "jwt-" + demo[2] + "-" + ts);
        data.put("refreshToken", "refresh-" + demo[2]);
        data.put("user",         userMap);
        data.put("message",      message);
        return data;
    }
}
