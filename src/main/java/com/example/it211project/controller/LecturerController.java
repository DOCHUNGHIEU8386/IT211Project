package com.example.it211project.controller;

import com.example.it211project.dto.request.GradeRequest;
import com.example.it211project.dto.response.ApiDataResponse;
import com.example.it211project.dto.response.GradeResponse;
import com.example.it211project.dto.response.LectureMaterialResponse;
import com.example.it211project.dto.response.SubmissionResponse;
import com.example.it211project.service.GradeService;
import com.example.it211project.service.LectureMaterialService;
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
@RequestMapping("/api/v1/lecturer")
@RequiredArgsConstructor
public class LecturerController {

    private final GradeService gradeService;
    private final SubmissionService submissionService;
    private final LectureMaterialService lectureMaterialService;

    // ==================== FR-08: Chấm điểm ====================

    /**
     * POST /api/v1/lecturer/grades
     * Chấm điểm bài nộp, ghi nhận xét/feedback
     */
    @PostMapping("/grades")
    public ResponseEntity<ApiDataResponse<GradeResponse>> grade(
            @Valid @RequestBody GradeRequest request) {

        return new ResponseEntity<>(
                new ApiDataResponse<>(
                        true,
                        "Grade submitted successfully",
                        gradeService.grade(request),
                        null,
                        HttpStatus.CREATED
                ),
                HttpStatus.CREATED
        );
    }

    /**
     * PUT /api/v1/lecturer/grades/{gradeId}
     * Cập nhật điểm / feedback
     */
    @PutMapping("/grades/{gradeId}")
    public ResponseEntity<ApiDataResponse<GradeResponse>> updateGrade(
            @PathVariable Long gradeId,
            @Valid @RequestBody GradeRequest request) {

        return ResponseEntity.ok(
                new ApiDataResponse<>(
                        true,
                        "Grade updated successfully",
                        gradeService.updateGrade(gradeId, request),
                        null,
                        HttpStatus.OK
                )
        );
    }

    /**
     * GET /api/v1/lecturer/grades/course/{courseId}
     * Xem tất cả điểm trong khóa học
     */
    @GetMapping("/grades/course/{courseId}")
    public ResponseEntity<ApiDataResponse<List<GradeResponse>>> getGradesByCourse(
            @PathVariable Long courseId) {

        return ResponseEntity.ok(
                new ApiDataResponse<>(
                        true,
                        "Get grades success",
                        gradeService.getGradesByCourse(courseId),
                        null,
                        HttpStatus.OK
                )
        );
    }

    /**
     * GET /api/v1/lecturer/grades/submission/{submissionId}
     * Xem điểm của một bài nộp cụ thể
     */
    @GetMapping("/grades/submission/{submissionId}")
    public ResponseEntity<ApiDataResponse<GradeResponse>> getGradeBySubmission(
            @PathVariable Long submissionId) {

        return ResponseEntity.ok(
                new ApiDataResponse<>(
                        true,
                        "Get grade success",
                        gradeService.getGradeBySubmission(submissionId),
                        null,
                        HttpStatus.OK
                )
        );
    }

    // ==================== FR-07 (view) / FR-08: Xem bài nộp ====================

    /**
     * GET /api/v1/lecturer/submissions/course/{courseId}
     * Xem danh sách bài nộp của tất cả sinh viên trong khóa học
     */
    @GetMapping("/submissions/course/{courseId}")
    public ResponseEntity<ApiDataResponse<List<SubmissionResponse>>> getSubmissionsByCourse(
            @PathVariable Long courseId) {

        return ResponseEntity.ok(
                new ApiDataResponse<>(
                        true,
                        "Get submissions success",
                        submissionService.getSubmissionsByCourse(courseId),
                        null,
                        HttpStatus.OK
                )
        );
    }

    /**
     * GET /api/v1/lecturer/submissions/{submissionId}
     * Xem chi tiết một bài nộp
     */
    @GetMapping("/submissions/{submissionId}")
    public ResponseEntity<ApiDataResponse<SubmissionResponse>> getSubmissionById(
            @PathVariable Long submissionId) {

        return ResponseEntity.ok(
                new ApiDataResponse<>(
                        true,
                        "Get submission success",
                        submissionService.getSubmissionById(submissionId),
                        null,
                        HttpStatus.OK
                )
        );
    }

    // ==================== FR-09: Tải lên tài liệu bài giảng ====================

    /**
     * POST /api/v1/lecturer/materials (multipart/form-data)
     * Giảng viên upload tài liệu bài giảng
     * Form fields: courseId, lecturerId, title, description (optional), file
     */
    @PostMapping(value = "/materials", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiDataResponse<LectureMaterialResponse>> uploadMaterial(
            @RequestParam @NotNull Long courseId,
            @RequestParam @NotNull Long lecturerId,
            @RequestParam @NotNull String title,
            @RequestParam(required = false) String description,
            @RequestParam("file") MultipartFile file) {

        return new ResponseEntity<>(
                new ApiDataResponse<>(
                        true,
                        "Material uploaded successfully",
                        lectureMaterialService.uploadMaterial(
                                courseId, lecturerId, title, description, file),
                        null,
                        HttpStatus.CREATED
                ),
                HttpStatus.CREATED
        );
    }

    /**
     * GET /api/v1/lecturer/materials/course/{courseId}
     * Xem danh sách tài liệu trong khóa học
     */
    @GetMapping("/materials/course/{courseId}")
    public ResponseEntity<ApiDataResponse<List<LectureMaterialResponse>>> getMaterialsByCourse(
            @PathVariable Long courseId) {

        return ResponseEntity.ok(
                new ApiDataResponse<>(
                        true,
                        "Get materials success",
                        lectureMaterialService.getMaterialsByCourse(courseId),
                        null,
                        HttpStatus.OK
                )
        );
    }

    /**
     * GET /api/v1/lecturer/materials/{materialId}
     * Xem chi tiết tài liệu
     */
    @GetMapping("/materials/{materialId}")
    public ResponseEntity<ApiDataResponse<LectureMaterialResponse>> getMaterialById(
            @PathVariable Long materialId) {

        return ResponseEntity.ok(
                new ApiDataResponse<>(
                        true,
                        "Get material success",
                        lectureMaterialService.getMaterialById(materialId),
                        null,
                        HttpStatus.OK
                )
        );
    }

    /**
     * DELETE /api/v1/lecturer/materials/{materialId}
     * Xóa tài liệu bài giảng
     */
    @DeleteMapping("/materials/{materialId}")
    public ResponseEntity<ApiDataResponse<String>> deleteMaterial(
            @PathVariable Long materialId) {

        lectureMaterialService.deleteMaterial(materialId);

        return ResponseEntity.ok(
                new ApiDataResponse<>(
                        true,
                        "Material deleted successfully",
                        "Material has been removed",
                        null,
                        HttpStatus.OK
                )
        );
    }
}
