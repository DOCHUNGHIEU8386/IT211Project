package com.example.it211project.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class SubmissionRequest {

    /** ID sinh viên nộp bài */
    @NotNull(message = "Student ID is required")
    private Long studentId;

    /** ID khóa học */
    @NotNull(message = "Course ID is required")
    private Long courseId;

    /**
     * Link GitHub (nếu nộp bằng link)
     * Phải bắt đầu bằng https://github.com/
     */
    private String githubLink;

    /** Ghi chú thêm */
    private String note;
}
