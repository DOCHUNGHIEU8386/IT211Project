package com.example.it211project.service;

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
import com.example.it211project.service.impl.EnrollmentServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class EnrollmentServiceTest {

    @Mock
    private EnrollmentRepository enrollmentRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CourseRepository courseRepository;

    @InjectMocks
    private EnrollmentServiceImpl enrollmentService;

    private EnrollmentRequest enrollmentRequest;
    private User student;
    private Course course;
    private Enrollment enrollment;

    @BeforeEach
    void setUp() {
        enrollmentRequest = EnrollmentRequest.builder()
                .studentId(2L)
                .courseId(1L)
                .build();

        student = User.builder()
                .id(2L)
                .username("teststudent")
                .fullName("Test Student")
                .build();

        course = Course.builder()
                .id(1L)
                .courseName("Java Programming")
                .description("Learn Java")
                .status(true)
                .build();

        enrollment = Enrollment.builder()
                .id(1L)
                .student(student)
                .course(course)
                .enrollDate(LocalDate.now())
                .build();
    }

    // ==================== Test Enroll - Success ====================
    @Test
    void enroll_ValidRequest_ReturnsEnrollmentResponse() {
        when(userRepository.findById(2L)).thenReturn(Optional.of(student));
        when(courseRepository.findById(1L)).thenReturn(Optional.of(course));
        when(enrollmentRepository.existsByStudentIdAndCourseId(2L, 1L)).thenReturn(false);
        when(enrollmentRepository.save(any(Enrollment.class))).thenReturn(enrollment);

        EnrollmentResponse result = enrollmentService.enroll(enrollmentRequest);

        assertThat(result).isNotNull();
        assertThat(result.getStudentId()).isEqualTo(2L);
        assertThat(result.getCourseName()).isEqualTo("Java Programming");

        verify(enrollmentRepository).save(any(Enrollment.class));
    }

    // ==================== Test Enroll - Student Not Found ====================
    @Test
    void enroll_StudentNotFound_ThrowsException() {
        when(userRepository.findById(2L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> enrollmentService.enroll(enrollmentRequest))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Student not found with id: 2");

        verify(enrollmentRepository, never()).save(any(Enrollment.class));
    }

    // ==================== Test Enroll - Course Not Found ====================
    @Test
    void enroll_CourseNotFound_ThrowsException() {
        when(userRepository.findById(2L)).thenReturn(Optional.of(student));
        when(courseRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> enrollmentService.enroll(enrollmentRequest))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Course not found with id: 1");

        verify(enrollmentRepository, never()).save(any(Enrollment.class));
    }

    // ==================== Test Enroll - Duplicate Enrollment ====================
    @Test
    void enroll_DuplicateEnrollment_ThrowsException() {
        when(userRepository.findById(2L)).thenReturn(Optional.of(student));
        when(courseRepository.findById(1L)).thenReturn(Optional.of(course));
        when(enrollmentRepository.existsByStudentIdAndCourseId(2L, 1L)).thenReturn(true);

        assertThatThrownBy(() -> enrollmentService.enroll(enrollmentRequest))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("Student already enrolled");

        verify(enrollmentRepository, never()).save(any(Enrollment.class));
    }

    // ==================== Test Get Enrollments By Student - Success ====================
    @Test
    void getEnrollmentsByStudent_ValidId_ReturnsList() {
        when(userRepository.existsById(2L)).thenReturn(true);
        when(enrollmentRepository.findByStudentId(2L)).thenReturn(List.of(enrollment));

        List<EnrollmentResponse> result = enrollmentService.getEnrollmentsByStudent(2L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getStudentName()).isEqualTo("Test Student");
        assertThat(result.get(0).getCourseName()).isEqualTo("Java Programming");
    }

    // ==================== Test Get Enrollments By Student - Not Found ====================
    @Test
    void getEnrollmentsByStudent_StudentNotFound_ThrowsException() {
        when(userRepository.existsById(999L)).thenReturn(false);

        assertThatThrownBy(() -> enrollmentService.getEnrollmentsByStudent(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Student not found with id: 999");
    }
}