package com.itcs383.restaurant.controller;

import com.itcs383.common.dto.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Lightweight auth endpoint served by restaurant-service.
 * Validates demo credentials and returns stable user data.
 *
 * Gateway routes /api/v1/auth/** → this service at /api/auth/**
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private static final String ROLE_CUSTOMER = "CUSTOMER";
    private static final String KEY_EMAIL     = "email";

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
        String password = req.getOrDefault("password", "");

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

    // POST /api/auth/register
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<Map<String, Object>>> register(
            @RequestBody Map<String, Object> req) {

        String email = (String) req.getOrDefault(KEY_EMAIL, "");
        String name  = (String) req.getOrDefault("name",  "New User");
        String role  = (String) req.getOrDefault("role",  ROLE_CUSTOMER);
        long   id    = (email.hashCode() & Integer.MAX_VALUE) % 900000L + 100000L;

        Map<String, Object> userMap = new HashMap<>();
        userMap.put("id",    id);
        userMap.put(KEY_EMAIL, email);
        userMap.put("name",  name);
        userMap.put("role",  role.toUpperCase());

        Map<String, Object> data = new HashMap<>();
        data.put("token",        "jwt-" + id);
        data.put("refreshToken", "refresh-" + id);
        data.put("user",         userMap);
        data.put("message",      "Registration successful");

        return ResponseEntity.status(201)
                .body(ApiResponse.success(data, "User registered successfully"));
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
