package com.example.it211project.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "grades")
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class Grade {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Liên kết tới bài nộp được chấm */
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "submission_id", unique = true, nullable = false)
    private Submission submission;

    /** Giảng viên chấm */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lecturer_id", nullable = false)
    private User lecturer;

    /** Điểm số (0 - 10) */
    @Column(nullable = false)
    private Double score;

    /** Nhận xét / Feedback của giảng viên */
    @Column(length = 3000)
    private String feedback;

    @Builder.Default
    private LocalDateTime gradedAt = LocalDateTime.now();
}
