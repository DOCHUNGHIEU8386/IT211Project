package com.example.it211project.service;

import com.example.it211project.dto.request.GradeRequest;
import com.example.it211project.dto.response.GradeResponse;
import com.example.it211project.entity.Course;
import com.example.it211project.entity.Grade;
import com.example.it211project.entity.Submission;
import com.example.it211project.entity.User;
import com.example.it211project.exception.DuplicateResourceException;
import com.example.it211project.exception.ResourceNotFoundException;
import com.example.it211project.repository.GradeRepository;
import com.example.it211project.repository.SubmissionRepository;
import com.example.it211project.repository.UserRepository;
import com.example.it211project.service.impl.GradeServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class GradeServiceTest {

    @Mock
    private GradeRepository gradeRepository;

    @Mock
    private SubmissionRepository submissionRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private GradeServiceImpl gradeService;

    private GradeRequest gradeRequest;
    private Submission submission;
    private User lecturer;
    private User student;
    private Course course;
    private Grade grade;
    private GradeResponse gradeResponse;

    @BeforeEach
    void setUp() {
        // Create course
        course = Course.builder()
                .id(1L)
                .courseName("Java Programming")
                .description("Learn Java")
                .status(true)
                .build();

        // Create student
        student = User.builder()
                .id(2L)
                .username("teststudent")
                .fullName("Test Student")
                .email("student@test.com")
                .enabled(true)
                .build();

        // Create lecturer
        lecturer = User.builder()
                .id(3L)
                .username("testlecturer")
                .fullName("Test Lecturer")
                .email("lecturer@test.com")
                .enabled(true)
                .build();

        // Create submission
        submission = Submission.builder()
                .id(1L)
                .student(student)
                .course(course)
                .githubLink("https://github.com/testuser/repo")
                .submissionType(Submission.SubmissionType.LINK)
                .submittedAt(LocalDateTime.now())
                .build();

        // Create grade request
        gradeRequest = GradeRequest.builder()
                .submissionId(1L)
                .lecturerId(3L)
                .score(8.5)
                .feedback("Good work, keep it up!")
                .build();

        // Create grade entity
        grade = Grade.builder()
                .id(1L)
                .submission(submission)
                .lecturer(lecturer)
                .score(8.5)
                .feedback("Good work, keep it up!")
                .gradedAt(LocalDateTime.now())
                .build();
    }

    // ==================== Test Grade - Success ====================
    @Test
    void grade_ValidRequest_ReturnsGradeResponse() {
        when(gradeRepository.existsBySubmissionId(1L)).thenReturn(false);
        when(submissionRepository.findById(1L)).thenReturn(Optional.of(submission));
        when(userRepository.findById(3L)).thenReturn(Optional.of(lecturer));
        when(gradeRepository.save(any(Grade.class))).thenReturn(grade);

        GradeResponse result = gradeService.grade(gradeRequest);

        assertThat(result).isNotNull();
        assertThat(result.getScore()).isEqualTo(8.5);
        assertThat(result.getFeedback()).isEqualTo("Good work, keep it up!");
        assertThat(result.getStudentName()).isEqualTo("Test Student");
        assertThat(result.getLecturerName()).isEqualTo("Test Lecturer");

        verify(gradeRepository).save(any(Grade.class));
    }

    // ==================== Test Grade - Already Graded ====================
    @Test
    void grade_AlreadyGraded_ThrowsException() {
        when(gradeRepository.existsBySubmissionId(1L)).thenReturn(true);

        assertThatThrownBy(() -> gradeService.grade(gradeRequest))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("has already been graded");

        verify(gradeRepository, never()).save(any(Grade.class));
    }

    // ==================== Test Grade - Submission Not Found ====================
    @Test
    void grade_SubmissionNotFound_ThrowsException() {
        when(gradeRepository.existsBySubmissionId(1L)).thenReturn(false);
        when(submissionRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> gradeService.grade(gradeRequest))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Submission not found");

        verify(gradeRepository, never()).save(any(Grade.class));
    }

    // ==================== Test Grade - Lecturer Not Found ====================
    @Test
    void grade_LecturerNotFound_ThrowsException() {
        when(gradeRepository.existsBySubmissionId(1L)).thenReturn(false);
        when(submissionRepository.findById(1L)).thenReturn(Optional.of(submission));
        when(userRepository.findById(3L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> gradeService.grade(gradeRequest))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Lecturer not found");

        verify(gradeRepository, never()).save(any(Grade.class));
    }

    // ==================== Test Update Grade - Success ====================
    @Test
    void updateGrade_ValidRequest_ReturnsUpdatedGrade() {
        Grade updatedGrade = Grade.builder()
                .id(1L)
                .submission(submission)
                .lecturer(lecturer)
                .score(9.0)
                .feedback("Excellent work!")
                .gradedAt(LocalDateTime.now())
                .build();

        GradeRequest updateRequest = GradeRequest.builder()
                .submissionId(1L)
                .lecturerId(3L)
                .score(9.0)
                .feedback("Excellent work!")
                .build();

        when(gradeRepository.findById(1L)).thenReturn(Optional.of(grade));
        when(gradeRepository.save(any(Grade.class))).thenReturn(updatedGrade);

        GradeResponse result = gradeService.updateGrade(1L, updateRequest);

        assertThat(result).isNotNull();
        assertThat(result.getScore()).isEqualTo(9.0);
        assertThat(result.getFeedback()).isEqualTo("Excellent work!");
    }

    // ==================== Test Update Grade - Not Found ====================
    @Test
    void updateGrade_GradeNotFound_ThrowsException() {
        when(gradeRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> gradeService.updateGrade(999L, gradeRequest))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Grade not found");
    }

    // ==================== Test Get Grade By Submission - Success ====================
    @Test
    void getGradeBySubmission_ValidId_ReturnsGrade() {
        when(gradeRepository.findBySubmissionId(1L)).thenReturn(Optional.of(grade));

        GradeResponse result = gradeService.getGradeBySubmission(1L);

        assertThat(result).isNotNull();
        assertThat(result.getSubmissionId()).isEqualTo(1L);
        assertThat(result.getScore()).isEqualTo(8.5);
    }

    // ==================== Test Get Grade By Submission - Not Found ====================
    @Test
    void getGradeBySubmission_NotFound_ThrowsException() {
        when(gradeRepository.findBySubmissionId(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> gradeService.getGradeBySubmission(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Grade not found");
    }

    // ==================== Test Get Grades By Student ====================
    @Test
    void getGradesByStudent_ValidId_ReturnsList() {
        when(gradeRepository.findBySubmission_Student_Id(2L)).thenReturn(List.of(grade));

        List<GradeResponse> result = gradeService.getGradesByStudent(2L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getStudentId()).isEqualTo(2L);
        assertThat(result.get(0).getScore()).isEqualTo(8.5);
    }

    // ==================== Test Get Grades By Course ====================
    @Test
    void getGradesByCourse_ValidId_ReturnsList() {
        when(gradeRepository.findBySubmission_Course_Id(1L)).thenReturn(List.of(grade));

        List<GradeResponse> result = gradeService.getGradesByCourse(1L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getCourseId()).isEqualTo(1L);
        assertThat(result.get(0).getCourseName()).isEqualTo("Java Programming");
    }

    // ==================== Test Get Grades By Student - Empty List ====================
    @Test
    void getGradesByStudent_NoGrades_ReturnsEmptyList() {
        when(gradeRepository.findBySubmission_Student_Id(2L)).thenReturn(List.of());

        List<GradeResponse> result = gradeService.getGradesByStudent(2L);

        assertThat(result).isEmpty();
    }
}