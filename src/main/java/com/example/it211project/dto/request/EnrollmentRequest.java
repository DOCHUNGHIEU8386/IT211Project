package com.example.it211project.dto.request;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class EnrollmentRequest {

    private Long studentId;

    private Long courseId;
}