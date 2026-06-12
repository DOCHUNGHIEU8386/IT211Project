package com.example.it211project.repository;

import com.example.it211project.entity.Grade;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface GradeRepository extends JpaRepository<Grade, Long> {

    Optional<Grade> findBySubmissionId(Long submissionId);

    /** Lấy tất cả điểm của sinh viên (qua submission.student.id) */
    List<Grade> findBySubmission_Student_Id(Long studentId);

    /** Lấy tất cả điểm trong một khóa học */
    List<Grade> findBySubmission_Course_Id(Long courseId);

    boolean existsBySubmissionId(Long submissionId);
}
