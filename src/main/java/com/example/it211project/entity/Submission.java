package com.example.it211project.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "submissions")
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class Submission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Sinh viên nộp bài */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private User student;

    /** Khóa học tương ứng */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    /**
     * Link GitHub hoặc URL file
     * FR-07: Sinh viên nộp link GitHub hoặc file
     */
    @Column(length = 1024)
    private String githubLink;

    /** Tên file đã upload (nếu dùng multipart) */
    private String fileName;

    /** Đường dẫn lưu file trên server */
    private String filePath;

    /** Loại nộp: LINK hoặc FILE */
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private SubmissionType submissionType = SubmissionType.LINK;

    /** Ghi chú của sinh viên */
    @Column(length = 2000)
    private String note;

    @Builder.Default
    private LocalDateTime submittedAt = LocalDateTime.now();

    public enum SubmissionType {
        LINK, FILE
    }
}
