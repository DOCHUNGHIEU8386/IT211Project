package com.example.it211project.controller;

import com.example.it211project.dto.request.RegisterRequest;
import com.example.it211project.exception.DuplicateResourceException;
import com.example.it211project.exception.ResourceNotFoundException;
import com.example.it211project.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class ExceptionHandlerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private UserService userService;

    // ==================== Test 404 - Resource Not Found ====================
    @Test
    void testResourceNotFoundException_returns404() throws Exception {
        doThrow(new ResourceNotFoundException("User not found with id: 999"))
                .when(userService).getUserById(999L);

        mockMvc.perform(get("/api/v1/admin/users/999")
                        .header("Authorization", "Bearer fake-token"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Resource not found"))
                .andExpect(jsonPath("$.errors").value("User not found with id: 999"));
    }

    // ==================== Test 409 - Duplicate Resource ====================
    @Test
    void testDuplicateResourceException_returns409() throws Exception {
        doThrow(new DuplicateResourceException("Username 'admin' already exists"))
                .when(userService).register(any(RegisterRequest.class));

        String requestJson = """
                {
                    "username": "admin",
                    "password": "123456",
                    "fullName": "Admin User",
                    "email": "admin@test.com"
                }
                """;

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Conflict"))
                .andExpect(jsonPath("$.errors").value("Username 'admin' already exists"));
    }

    // ==================== Test 400 - Validation Error ====================
    @Test
    void testValidationException_returns400() throws Exception {
        String requestJson = """
                {
                    "username": "testuser",
                    "fullName": "Test User"
                }
                """;

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Validation failed"));
    }

    // ==================== Test 401 - Token Expired ====================
    @Test
    void testTokenExpiredException_returns401() throws Exception {
        String expiredToken = "eyJhbGciOiJIUzI1NiJ9.expired-token";

        mockMvc.perform(get("/api/v1/admin/users")
                        .header("Authorization", "Bearer " + expiredToken))
                .andExpect(status().isUnauthorized());
    }

    // ==================== Test 403 - Forbidden ====================
    @Test
    void testForbiddenException_returns403() throws Exception {
        String studentToken = "eyJhbGciOiJIUzI1NiJ9.student-token";

        mockMvc.perform(get("/api/v1/admin/users")
                        .header("Authorization", "Bearer " + studentToken))
                .andExpect(status().isForbidden());
    }

    // ==================== Test 500 - Internal Server Error ====================
    @Test
    void testGeneralException_returns500() throws Exception {
        doThrow(new RuntimeException("Unexpected database error"))
                .when(userService).getUserById(any(Long.class));

        mockMvc.perform(get("/api/v1/admin/users/1")
                        .header("Authorization", "Bearer fake-token"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Internal server error"));
    }
}