package com.example.it211project.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class CourseRequest {

    @NotBlank(message = "Course name is required")
    private String courseName;

    private String description;

    private Boolean status;
}