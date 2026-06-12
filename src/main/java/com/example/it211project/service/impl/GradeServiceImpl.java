package com.example.it211project.service.impl;

import com.example.it211project.dto.request.GradeRequest;
import com.example.it211project.dto.response.GradeResponse;
import com.example.it211project.entity.Grade;
import com.example.it211project.entity.Submission;
import com.example.it211project.entity.User;
import com.example.it211project.exception.DuplicateResourceException;
import com.example.it211project.exception.ResourceNotFoundException;
import com.example.it211project.repository.GradeRepository;
import com.example.it211project.repository.SubmissionRepository;
import com.example.it211project.repository.UserRepository;
import com.example.it211project.service.GradeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GradeServiceImpl implements GradeService {

    private final GradeRepository gradeRepository;
    private final SubmissionRepository submissionRepository;
    private final UserRepository userRepository;

    // ==================== FR-08: Chấm điểm ====================
    @Override
    public GradeResponse grade(GradeRequest request) {
        // Kiểm tra đã chấm chưa
        if (gradeRepository.existsBySubmissionId(request.getSubmissionId())) {
            throw new DuplicateResourceException(
                    "Submission id=" + request.getSubmissionId() + " has already been graded. Use update instead.");
        }

        Submission submission = getSubmission(request.getSubmissionId());
        User lecturer = getLecturer(request.getLecturerId());

        Grade grade = Grade.builder()
                .submission(submission)
                .lecturer(lecturer)
                .score(request.getScore())
                .feedback(request.getFeedback())
                .gradedAt(LocalDateTime.now())
                .build();

        return mapToResponse(gradeRepository.save(grade));
    }

    // ==================== FR-08: Cập nhật điểm / feedback ====================
    @Override
    public GradeResponse updateGrade(Long gradeId, GradeRequest request) {
        Grade grade = gradeRepository.findById(gradeId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Grade not found with id: " + gradeId));

        grade.setScore(request.getScore());
        grade.setFeedback(request.getFeedback());
        grade.setGradedAt(LocalDateTime.now());

        return mapToResponse(gradeRepository.save(grade));
    }

    @Override
    public GradeResponse getGradeBySubmission(Long submissionId) {
        return mapToResponse(gradeRepository.findBySubmissionId(submissionId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Grade not found for submission id: " + submissionId)));
    }

    @Override
    public List<GradeResponse> getGradesByStudent(Long studentId) {
        return gradeRepository.findBySubmission_Student_Id(studentId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<GradeResponse> getGradesByCourse(Long courseId) {
        return gradeRepository.findBySubmission_Course_Id(courseId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // ==================== Helper methods ====================

    private Submission getSubmission(Long submissionId) {
        return submissionRepository.findById(submissionId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Submission not found with id: " + submissionId));
    }

    private User getLecturer(Long lecturerId) {
        return userRepository.findById(lecturerId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Lecturer not found with id: " + lecturerId));
    }

    private GradeResponse mapToResponse(Grade g) {
        Submission s = g.getSubmission();
        return GradeResponse.builder()
                .id(g.getId())
                .submissionId(s.getId())
                .studentId(s.getStudent().getId())
                .studentName(s.getStudent().getFullName())
                .courseId(s.getCourse().getId())
                .courseName(s.getCourse().getCourseName())
                .lecturerId(g.getLecturer().getId())
                .lecturerName(g.getLecturer().getFullName())
                .score(g.getScore())
                .feedback(g.getFeedback())
                .gradedAt(g.getGradedAt())
                .build();
    }
}
