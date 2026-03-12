package com.itcs383.restaurant.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.itcs383.restaurant.config.JpaConfig;
import com.itcs383.restaurant.service.UserService;

@WebMvcTest(controllers = AuthController.class,
    excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = JpaConfig.class))
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @BeforeEach
    void setUp() {
        Mockito.when(userService.emailExists(Mockito.anyString())).thenReturn(false);
        Mockito.when(userService.phoneExists(Mockito.anyString())).thenReturn(false);
        Mockito.when(userService.createUser(Mockito.anyMap())).thenReturn(42L);
    }

    // POST /api/auth/login — valid credentials
    @Test
    void login_WithValidCredentials_ShouldReturn200() throws Exception {
        String body = "{\"email\":\"customer@foodexpress.com\",\"password\":\"Customer123!\"}";
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.token").exists())
                .andExpect(jsonPath("$.data.user.role").value("CUSTOMER"));
    }

    // POST /api/auth/login — wrong password
    @Test
    void login_WithInvalidPassword_ShouldReturn401() throws Exception {
        String body = "{\"email\":\"customer@foodexpress.com\",\"password\":\"WrongPassword!\"}";
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false));
    }

    // POST /api/auth/login — unknown email
    @Test
    void login_WithUnknownEmail_ShouldReturn401() throws Exception {
        String body = "{\"email\":\"unknown@example.com\",\"password\":\"AnyPass!\"}";
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false));
    }

    // POST /api/auth/otp — valid known email
    @Test
    void verifyOtp_WithKnownEmail_ShouldReturn200() throws Exception {
        String body = "{\"email\":\"customer@foodexpress.com\",\"otp\":\"123456\"}";
        mockMvc.perform(post("/api/auth/otp")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.user.email").value("customer@foodexpress.com"))
                .andExpect(jsonPath("$.data.user.role").value("CUSTOMER"));
    }

    // POST /api/auth/otp — unknown email
    @Test
    void verifyOtp_WithUnknownEmail_ShouldReturn401() throws Exception {
        String body = "{\"email\":\"nobody@example.com\",\"otp\":\"000000\"}";
        mockMvc.perform(post("/api/auth/otp")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false));
    }

    // POST /api/auth/register — new user
    @Test
    void register_ShouldReturn201WithUserData() throws Exception {
        String body = "{\"email\":\"newuser@example.com\",\"name\":\"New User\",\"role\":\"customer\"}";
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.token").exists())
                .andExpect(jsonPath("$.data.user.email").value("newuser@example.com"))
                .andExpect(jsonPath("$.data.user.role").value("CUSTOMER"));
    }

    // GET /api/auth/me
    @Test
    void me_ShouldReturn200WithStatusOk() throws Exception {
        mockMvc.perform(get("/api/auth/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("ok"));
    }

    // POST /api/auth/login — rider role
    @Test
    void login_WithRiderCredentials_ShouldReturnRiderRole() throws Exception {
        String body = "{\"email\":\"rider@foodexpress.com\",\"password\":\"Rider123!\"}";
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.user.role").value("RIDER"));
    }

    // POST /api/auth/login — admin role
    @Test
    void login_WithAdminCredentials_ShouldReturnAdminRole() throws Exception {
        String body = "{\"email\":\"admin@foodexpress.com\",\"password\":\"Admin123!\"}";
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.user.role").value("ADMIN"));
    }
}
