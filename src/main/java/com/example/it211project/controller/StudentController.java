package com.example.it211project.controller;

import com.example.it211project.dto.request.EnrollmentRequest;
import com.example.it211project.dto.response.ApiDataResponse;
import com.example.it211project.dto.response.EnrollmentResponse;
import com.example.it211project.service.EnrollmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/student")
@RequiredArgsConstructor
public class StudentController {

    private final EnrollmentService enrollmentService;

    /**
     * FR-06: Sinh viên đăng ký khóa học
     * POST /api/v1/student/enrollments
     * Không cho đăng ký trùng khóa học
     */
    @PostMapping("/enrollments")
    public ResponseEntity<ApiDataResponse<EnrollmentResponse>> enrollCourse(
            @RequestBody EnrollmentRequest request) {

        return new ResponseEntity<>(
                new ApiDataResponse<>(
                        true,
                        "Enroll course success",
                        enrollmentService.enroll(request),
                        null,
                        HttpStatus.CREATED
                ),
                HttpStatus.CREATED
        );
    }

    /**
     * GET /api/v1/student/enrollments/{studentId}
     * Xem danh sách khóa học đã đăng ký
     */
    @GetMapping("/enrollments/{studentId}")
    public ResponseEntity<ApiDataResponse<List<EnrollmentResponse>>> getMyEnrollments(
            @PathVariable Long studentId) {

        return ResponseEntity.ok(
                new ApiDataResponse<>(
                        true,
                        "Get enrollments success",
                        enrollmentService.getEnrollmentsByStudent(studentId),
                        null,
                        HttpStatus.OK
                )
        );
    }
}