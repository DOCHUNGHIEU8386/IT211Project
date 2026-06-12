package com.example.it211project.dto.request;

import jakarta.validation.constraints.*;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class GradeRequest {

    /** ID bài nộp cần chấm */
    @NotNull(message = "Submission ID is required")
    private Long submissionId;

    /** ID giảng viên chấm */
    @NotNull(message = "Lecturer ID is required")
    private Long lecturerId;

    /** Điểm số (0 - 10) */
    @NotNull(message = "Score is required")
    @DecimalMin(value = "0.0", message = "Score must be >= 0")
    @DecimalMax(value = "10.0", message = "Score must be <= 10")
    private Double score;

    /** Nhận xét / Feedback */
    @Size(max = 3000, message = "Feedback cannot exceed 3000 characters")
    private String feedback;
}
