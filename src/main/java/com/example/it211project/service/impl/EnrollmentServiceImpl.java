package com.example.it211project.service.impl;

import com.example.it211project.dto.request.EnrollmentRequest;
import com.example.it211project.dto.response.EnrollmentResponse;
import com.example.it211project.entity.Course;
import com.example.it211project.entity.Enrollment;
import com.example.it211project.entity.User;
import com.example.it211project.exception.DuplicateResourceException;
import com.example.it211project.exception.ResourceNotFoundException;
import com.example.it211project.repository.CourseRepository;
import com.example.it211project.repository.EnrollmentRepository;
import com.example.it211project.repository.UserRepository;
import com.example.it211project.service.EnrollmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EnrollmentServiceImpl implements EnrollmentService {

    private final EnrollmentRepository enrollmentRepository;
    private final UserRepository userRepository;
    private final CourseRepository courseRepository;

    // ==================== FR-06: Enroll course (no duplicate) ====================
    @Override
    public EnrollmentResponse enroll(EnrollmentRequest request) {

        User student = userRepository.findById(request.getStudentId())
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Student not found with id: " + request.getStudentId()));

        Course course = courseRepository.findById(request.getCourseId())
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Course not found with id: " + request.getCourseId()));

        // FR-06: Không cho đăng ký trùng khóa học
        if (enrollmentRepository.existsByStudentIdAndCourseId(
                request.getStudentId(), request.getCourseId())) {
            throw new DuplicateResourceException(
                    "Student already enrolled in course: " + course.getCourseName());
        }

        Enrollment enrollment = Enrollment.builder()
                .student(student)
                .course(course)
                .enrollDate(LocalDate.now())
                .build();

        return mapToResponse(enrollmentRepository.save(enrollment));
    }

    // ==================== Get enrollments by student ====================
    @Override
    public List<EnrollmentResponse> getEnrollmentsByStudent(Long studentId) {
        if (!userRepository.existsById(studentId)) {
            throw new ResourceNotFoundException("Student not found with id: " + studentId);
        }

        return enrollmentRepository.findByStudentId(studentId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // ==================== Helper: Entity -> DTO ====================
    private EnrollmentResponse mapToResponse(Enrollment enrollment) {
        return EnrollmentResponse.builder()
                .id(enrollment.getId())
                .studentId(enrollment.getStudent().getId())
                .studentName(enrollment.getStudent().getFullName())
                .courseId(enrollment.getCourse().getId())
                .courseName(enrollment.getCourse().getCourseName())
                .enrollDate(enrollment.getEnrollDate())
                .build();
    }
}