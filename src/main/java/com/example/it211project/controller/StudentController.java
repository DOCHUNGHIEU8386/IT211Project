package com.example.it211project.controller;

import com.example.it211project.dto.request.EnrollmentRequest;
import com.example.it211project.dto.request.SubmissionRequest;
import com.example.it211project.dto.response.ApiDataResponse;
import com.example.it211project.dto.response.EnrollmentResponse;
import com.example.it211project.dto.response.GradeResponse;
import com.example.it211project.dto.response.SubmissionResponse;
import com.example.it211project.service.EnrollmentService;
import com.example.it211project.service.GradeService;
import com.example.it211project.service.SubmissionService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/v1/student")
@RequiredArgsConstructor
public class StudentController {

    private final EnrollmentService enrollmentService;
    private final SubmissionService submissionService;
    private final GradeService gradeService;

    // ==================== FR-06: Đăng ký khóa học ====================
    /**
     * POST /api/v1/student/enrollments
     * Sinh viên đăng ký khóa học – không cho đăng ký trùng
     */
    @PostMapping("/enrollments")
    public ResponseEntity<ApiDataResponse<EnrollmentResponse>> enrollCourse(
            @Valid @RequestBody EnrollmentRequest request) {

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

    // ==================== FR-07: Nộp bài bằng GitHub link ====================
    /**
     * POST /api/v1/student/submissions/link
     * Nộp bài tập bằng link GitHub
     */
    @PostMapping("/submissions/link")
    public ResponseEntity<ApiDataResponse<SubmissionResponse>> submitByLink(
            @Valid @RequestBody SubmissionRequest request) {

        return new ResponseEntity<>(
                new ApiDataResponse<>(
                        true,
                        "Submission submitted successfully",
                        submissionService.submitByLink(request),
                        null,
                        HttpStatus.CREATED
                ),
                HttpStatus.CREATED
        );
    }

    // ==================== FR-07: Nộp bài bằng file upload ====================
    /**
     * POST /api/v1/student/submissions/file
     * Nộp bài tập bằng cách upload file (multipart/form-data)
     * Form fields: studentId, courseId, note (optional), file
     */
    @PostMapping(value = "/submissions/file", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiDataResponse<SubmissionResponse>> submitByFile(
            @RequestParam @NotNull Long studentId,
            @RequestParam @NotNull Long courseId,
            @RequestParam(required = false) String note,
            @RequestParam("file") MultipartFile file) {

        return new ResponseEntity<>(
                new ApiDataResponse<>(
                        true,
                        "File submitted successfully",
                        submissionService.submitByFile(studentId, courseId, note, file),
                        null,
                        HttpStatus.CREATED
                ),
                HttpStatus.CREATED
        );
    }

    /**
     * GET /api/v1/student/submissions/{studentId}
     * Xem danh sách bài đã nộp của sinh viên
     */
    @GetMapping("/submissions/{studentId}")
    public ResponseEntity<ApiDataResponse<List<SubmissionResponse>>> getMySubmissions(
            @PathVariable Long studentId) {

        return ResponseEntity.ok(
                new ApiDataResponse<>(
                        true,
                        "Get submissions success",
                        submissionService.getSubmissionsByStudent(studentId),
                        null,
                        HttpStatus.OK
                )
        );
    }

    /**
     * GET /api/v1/student/grades/{studentId}
     * Xem điểm của sinh viên
     */
    @GetMapping("/grades/{studentId}")
    public ResponseEntity<ApiDataResponse<List<GradeResponse>>> getMyGrades(
            @PathVariable Long studentId) {

        return ResponseEntity.ok(
                new ApiDataResponse<>(
                        true,
                        "Get grades success",
                        gradeService.getGradesByStudent(studentId),
                        null,
                        HttpStatus.OK
                )
        );
    }
}