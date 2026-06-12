package com.example.it211project.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "lecture_materials")
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class LectureMaterial {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Khóa học chứa tài liệu */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    /** Giảng viên upload */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lecturer_id", nullable = false)
    private User lecturer;

    /** Tiêu đề tài liệu */
    @Column(nullable = false)
    private String title;

    /** Mô tả */
    @Column(length = 2000)
    private String description;

    /** Tên file gốc */
    private String fileName;

    /** Đường dẫn file lưu trên server */
    private String filePath;

    /** Loại file (pdf, pptx, docx, ...) */
    private String fileType;

    /** Kích thước file (byte) */
    private Long fileSize;

    @Builder.Default
    private LocalDateTime uploadedAt = LocalDateTime.now();
}
