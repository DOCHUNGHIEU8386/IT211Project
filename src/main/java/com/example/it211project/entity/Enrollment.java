    package com.example.it211project.entity;

    import jakarta.persistence.*;
    import lombok.*;

    import java.time.LocalDate;

    @Entity
    @Table(name = "enrollments")
    @AllArgsConstructor
    @NoArgsConstructor
    @Data
    @Builder
    public class Enrollment {

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

        @ManyToOne
        @JoinColumn(name="student_id")
        private User student;

        @ManyToOne
        @JoinColumn(name="course_id")
        private Course course;

        private LocalDate enrollDate;
    }