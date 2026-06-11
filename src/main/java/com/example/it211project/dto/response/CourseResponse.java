package com.example.it211project.dto.response;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class CourseResponse {

    private Long id;
    private String courseName;
    private String description;
    private Boolean status;
}
