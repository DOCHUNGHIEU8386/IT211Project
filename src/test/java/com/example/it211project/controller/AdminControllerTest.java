package com.example.it211project.controller;

import com.example.it211project.dto.request.CourseRequest;
import com.example.it211project.dto.response.CourseResponse;
import com.example.it211project.dto.response.UserResponse;
import com.example.it211project.service.CourseService;
import com.example.it211project.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class AdminControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean  // Thay thế @MockBean
    private UserService userService;

    @MockitoBean  // Thay thế @MockBean
    private CourseService courseService;

    private String adminToken;
    private UserResponse userResponse;
    private CourseResponse courseResponse;
    private CourseRequest courseRequest;

    @BeforeEach
    void setUp() {
        adminToken = "eyJhbGciOiJIUzI1NiJ9.mock-admin-token";

        userResponse = UserResponse.builder()
                .id(1L)
                .username("admin")
                .fullName("Administrator")
                .email("admin@it211.com")
                .enabled(true)
                .role("ROLE_ADMIN")
                .build();

        courseResponse = CourseResponse.builder()
                .id(1L)
                .courseName("Java Spring Boot")
                .description("Learn Spring Boot framework")
                .status(true)
                .build();

        courseRequest = CourseRequest.builder()
                .courseName("Python Programming")
                .description("Learn Python basics")
                .status(true)
                .build();
    }

    @Test
    void getUsers_ValidRequest_Returns200() throws Exception {
        Page<UserResponse> userPage = new PageImpl<>(List.of(userResponse));

        when(userService.getAllUsers(anyInt(), anyInt(), isNull())).thenReturn(userPage);

        mockMvc.perform(get("/api/v1/admin/users")
                        .header("Authorization", "Bearer " + adminToken)
                        .param("page", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Get users success"));
    }

    @Test
    void createCourse_ValidRequest_Returns201() throws Exception {
        CourseResponse savedCourse = CourseResponse.builder()
                .id(2L)
                .courseName("Python Programming")
                .description("Learn Python basics")
                .status(true)
                .build();

        when(courseService.save(any(CourseRequest.class))).thenReturn(savedCourse);

        mockMvc.perform(post("/api/v1/admin/courses")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(courseRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Create course success"));
    }
}