package com.example.it211project.dto.response;

import lombok.*;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class GradeResponse {

    private Long id;
    private Long submissionId;
    private Long studentId;
    private String studentName;
    private Long courseId;
    private String courseName;
    private Long lecturerId;
    private String lecturerName;
    private Double score;
    private String feedback;
    private LocalDateTime gradedAt;
}
