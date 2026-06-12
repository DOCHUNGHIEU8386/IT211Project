package com.example.it211project.controller;

import com.example.it211project.dto.request.LoginRequest;
import com.example.it211project.dto.request.RegisterRequest;
import com.example.it211project.dto.response.TokenResponse;
import com.example.it211project.dto.response.UserResponse;
import com.example.it211project.entity.User;
import com.example.it211project.repository.UserRepository;
import com.example.it211project.security.jwt.JWTProvider;
import com.example.it211project.service.RefreshTokenService;
import com.example.it211project.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private AuthenticationManager authenticationManager;

    @MockitoBean
    private JWTProvider jwtProvider;

    @MockitoBean
    private RefreshTokenService refreshTokenService;

    @MockitoBean
    private UserRepository userRepository;

    private RegisterRequest validRegisterRequest;
    private LoginRequest validLoginRequest;
    private UserResponse userResponse;
    private TokenResponse tokenResponse;

    @BeforeEach
    void setUp() {
        validRegisterRequest = RegisterRequest.builder()
                .username("teststudent")
                .password("123456")
                .fullName("Test Student")
                .email("test@example.com")
                .build();

        userResponse = UserResponse.builder()
                .id(1L)
                .username("teststudent")
                .fullName("Test Student")
                .email("test@example.com")
                .enabled(true)
                .role("ROLE_STUDENT")
                .build();

        validLoginRequest = LoginRequest.builder()
                .username("teststudent")
                .password("123456")
                .build();

        tokenResponse = TokenResponse.builder()
                .accessToken("eyJhbGciOiJIUzI1NiJ9.test-access-token")
                .refreshToken("test-refresh-token-uuid")
                .tokenType("Bearer")
                .build();
    }

    @Test
    void register_ValidRequest_Returns201() throws Exception {
        when(userService.register(any(RegisterRequest.class))).thenReturn(userResponse);

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRegisterRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Register successful"));
    }
}