package com.example.it211project.dto.response;

import lombok.*;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class LectureMaterialResponse {

    private Long id;
    private Long courseId;
    private String courseName;
    private Long lecturerId;
    private String lecturerName;
    private String title;
    private String description;
    private String fileName;
    private String fileType;
    private Long fileSize;
    private LocalDateTime uploadedAt;
}
