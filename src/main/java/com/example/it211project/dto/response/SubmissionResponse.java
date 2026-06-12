package com.example.it211project.dto.response;

import com.example.it211project.entity.Submission;
import lombok.*;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class SubmissionResponse {

    private Long id;
    private Long studentId;
    private String studentName;
    private Long courseId;
    private String courseName;
    private String githubLink;
    private String fileName;
    private Submission.SubmissionType submissionType;
    private String note;
    private LocalDateTime submittedAt;
}
