package com.example.it211project.controller;

import com.example.it211project.dto.request.CourseRequest;
import com.example.it211project.dto.request.LoginRequest;
import com.example.it211project.dto.request.UserUpdateRequest;
import com.example.it211project.dto.response.ApiDataResponse;
import com.example.it211project.dto.response.CourseResponse;
import com.example.it211project.dto.response.LoginResponse;
import com.example.it211project.dto.response.UserResponse;
import com.example.it211project.service.CourseService;
import com.example.it211project.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import com.example.it211project.security.jwt.JWTProvider;

@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
public class AdminController {

    private final UserService userService;
    private final CourseService courseService;

    @Value("${pageSize}")
    private Integer pageSize;

    // ==================== FR-05: User CRUD ====================

    /**
     * GET /api/v1/admin/users?page=1
     * Lấy danh sách user có phân trang
     */
    @GetMapping("/users")
    public ResponseEntity<ApiDataResponse<Page<UserResponse>>> getUsers(
            @RequestParam(defaultValue = "1") Integer page) {

        return ResponseEntity.ok(
                new ApiDataResponse<>(
                        true,
                        "Get users success",
                        userService.getAllUsers(page - 1, pageSize),
                        null,
                        HttpStatus.OK
                )
        );
    }

    /**
     * GET /api/v1/admin/users/{id}
     * Lấy thông tin user theo ID
     */
    @GetMapping("/users/{id}")
    public ResponseEntity<ApiDataResponse<UserResponse>> getUserById(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                new ApiDataResponse<>(
                        true,
                        "Get user success",
                        userService.getUserById(id),
                        null,
                        HttpStatus.OK
                )
        );
    }

    /**
     * PUT /api/v1/admin/users/{id}
     * Cập nhật thông tin user
     */
    @PutMapping("/users/{id}")
    public ResponseEntity<ApiDataResponse<UserResponse>> updateUser(
            @PathVariable Long id,
            @Valid @RequestBody UserUpdateRequest request) {

        return ResponseEntity.ok(
                new ApiDataResponse<>(
                        true,
                        "Update user success",
                        userService.updateUser(id, request),
                        null,
                        HttpStatus.OK
                )
        );
    }

    /**
     * DELETE /api/v1/admin/users/{id}
     * Xóa user
     */
    @DeleteMapping("/users/{id}")
    public ResponseEntity<ApiDataResponse<String>> deleteUser(
            @PathVariable Long id) {

        userService.deleteUser(id);

        return ResponseEntity.ok(
                new ApiDataResponse<>(
                        true,
                        "Delete user success",
                        "User deleted successfully",
                        null,
                        HttpStatus.OK
                )
       );
    }

    // ==================== FR-05: Course CRUD ====================

    /**
     * GET /api/v1/admin/courses?page=1&keyword=java
     * Lấy danh sách course có phân trang và search theo tên
     */
    @GetMapping("/courses")
    public ResponseEntity<ApiDataResponse<Page<CourseResponse>>> getCourses(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(required = false) String keyword) {

        return ResponseEntity.ok(
                new ApiDataResponse<>(
                        true,
                        "Get courses success",
                        courseService.getCourses(page - 1, pageSize, keyword),
                        null,
                        HttpStatus.OK
                )
        );
    }

    /**
     * GET /api/v1/admin/courses/{id}
     * Lấy thông tin course theo ID
     */
    @GetMapping("/courses/{id}")
    public ResponseEntity<ApiDataResponse<CourseResponse>> getCourseById(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                new ApiDataResponse<>(
                        true,
                        "Get course success",
                        courseService.getCourseById(id),
                        null,
                        HttpStatus.OK
                )
        );
    }

    /**
     * POST /api/v1/admin/courses
     * Tạo khóa học mới
     */
    @PostMapping("/courses")
    public ResponseEntity<ApiDataResponse<CourseResponse>> addCourse(
            @Valid @RequestBody CourseRequest request) {

        return new ResponseEntity<>(
                new ApiDataResponse<>(
                        true,
                        "Create course success",
                        courseService.save(request),
                        null,
                        HttpStatus.CREATED
                ),
                HttpStatus.CREATED
        );
    }

    /**
     * PUT /api/v1/admin/courses/{id}
     * Cập nhật khóa học
     */
    @PutMapping("/courses/{id}")
    public ResponseEntity<ApiDataResponse<CourseResponse>> updateCourse(
            @PathVariable Long id,
            @Valid @RequestBody CourseRequest request) {

        return ResponseEntity.ok(
                new ApiDataResponse<>(
                        true,
                        "Update course success",
                        courseService.update(id, request),
                        null,
                        HttpStatus.OK
                )
        );
    }

    /**
     * DELETE /api/v1/admin/courses/{id}
     * Xóa khóa học
     */
    @DeleteMapping("/courses/{id}")
    public ResponseEntity<ApiDataResponse<String>> deleteCourse(
            @PathVariable Long id) {

        courseService.delete(id);

        return ResponseEntity.ok(
                new ApiDataResponse<>(
                        true,
                        "Delete course success",
                        "Course deleted successfully",
                        null,
                        HttpStatus.OK
                )
        );
    }

}