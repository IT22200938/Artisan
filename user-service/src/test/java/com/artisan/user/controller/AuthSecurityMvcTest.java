package com.artisan.user.controller;

import com.artisan.user.config.JwtAuthFilter;
import com.artisan.user.config.JwtUtil;
import com.artisan.user.config.SecurityConfig;
import com.artisan.user.dto.AuthResponse;
import com.artisan.user.service.AuthService;
import com.artisan.user.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = {AuthController.class, UserController.class})
@Import({SecurityConfig.class, JwtAuthFilter.class})
class AuthSecurityMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuthService authService;

    @MockBean
    private UserService userService;

    @MockBean
    private JwtUtil jwtUtil;

    @Test
    void login_shouldAllowAnonymousPostWithoutCsrf() throws Exception {
        when(authService.login(any())).thenReturn(AuthResponse.builder()
                .token("token")
                .userId("user-1")
                .displayName("Test User")
                .role("BUYER")
                .build());

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "test@example.com",
                                  "password": "password123"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("token"));
    }

    @Test
    void register_shouldAllowAnonymousPostWithoutCsrf() throws Exception {
        when(authService.register(any())).thenReturn(AuthResponse.builder()
                .token("token")
                .userId("user-1")
                .displayName("Test User")
                .role("BUYER")
                .build());

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "test@example.com",
                                  "password": "password123",
                                  "displayName": "Test User",
                                  "role": "BUYER"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").value("token"));
    }

    @Test
    void updateProfile_shouldRequireAuthentication() throws Exception {
        mockMvc.perform(put("/api/users/user-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "displayName": "Updated Name"
                                }
                                """))
                .andExpect(status().isUnauthorized());
    }
}
