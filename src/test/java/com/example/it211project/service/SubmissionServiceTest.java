package com.example.it211project.service;

import com.example.it211project.dto.request.SubmissionRequest;
import com.example.it211project.dto.response.SubmissionResponse;
import com.example.it211project.entity.Course;
import com.example.it211project.entity.Submission;
import com.example.it211project.entity.User;
import com.example.it211project.exception.DuplicateResourceException;
import com.example.it211project.exception.ResourceNotFoundException;
import com.example.it211project.repository.CourseRepository;
import com.example.it211project.repository.SubmissionRepository;
import com.example.it211project.repository.UserRepository;
import com.example.it211project.service.impl.SubmissionServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class SubmissionServiceTest {

    @Mock
    private SubmissionRepository submissionRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CourseRepository courseRepository;

    @InjectMocks
    private SubmissionServiceImpl submissionService;

    private SubmissionRequest linkRequest;
    private User student;
    private Course course;
    private Submission submission;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(submissionService, "submissionDir", "uploads/submissions");

        linkRequest = SubmissionRequest.builder()
                .studentId(2L)
                .courseId(1L)
                .githubLink("https://github.com/testuser/assignment")
                .note("My assignment")
                .build();

        student = User.builder()
                .id(2L)
                .username("teststudent")
                .fullName("Test Student")
                .build();

        course = Course.builder()
                .id(1L)
                .courseName("Java Programming")
                .build();

        submission = Submission.builder()
                .id(1L)
                .student(student)
                .course(course)
                .githubLink("https://github.com/testuser/assignment")
                .submissionType(Submission.SubmissionType.LINK)
                .submittedAt(LocalDateTime.now())
                .build();
    }

    @Test
    void submitByLink_ValidRequest_ReturnsSubmissionResponse() {
        when(userRepository.findById(2L)).thenReturn(Optional.of(student));
        when(courseRepository.findById(1L)).thenReturn(Optional.of(course));
        when(submissionRepository.existsByStudentIdAndCourseId(2L, 1L)).thenReturn(false);
        when(submissionRepository.save(any(Submission.class))).thenReturn(submission);

        SubmissionResponse result = submissionService.submitByLink(linkRequest);

        assertThat(result).isNotNull();
        assertThat(result.getStudentId()).isEqualTo(2L);
        assertThat(result.getGithubLink()).isEqualTo("https://github.com/testuser/assignment");
        assertThat(result.getSubmissionType()).isEqualTo(Submission.SubmissionType.LINK);
    }

    @Test
    void submitByLink_InvalidGithubUrl_ThrowsException() {
        linkRequest.setGithubLink("https://gitlab.com/testuser/assignment");

        assertThatThrownBy(() -> submissionService.submitByLink(linkRequest))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("GitHub link must start with https://github.com/");
    }

    @Test
    void submitByLink_MissingGithubLink_ThrowsException() {
        linkRequest.setGithubLink(null);

        assertThatThrownBy(() -> submissionService.submitByLink(linkRequest))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("GitHub link is required");
    }

    @Test
    void submitByLink_StudentNotFound_ThrowsException() {
        when(userRepository.findById(2L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> submissionService.submitByLink(linkRequest))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Student not found");
    }

    @Test
    void submitByLink_DuplicateSubmission_ThrowsException() {
        when(userRepository.findById(2L)).thenReturn(Optional.of(student));
        when(courseRepository.findById(1L)).thenReturn(Optional.of(course));
        when(submissionRepository.existsByStudentIdAndCourseId(2L, 1L)).thenReturn(true);

        assertThatThrownBy(() -> submissionService.submitByLink(linkRequest))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("already submitted");
    }

    @Test
    void getSubmissionsByStudent_ValidId_ReturnsList() {
        when(userRepository.existsById(2L)).thenReturn(true);
        when(submissionRepository.findByStudentId(2L)).thenReturn(List.of(submission));

        List<SubmissionResponse> result = submissionService.getSubmissionsByStudent(2L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getGithubLink()).isEqualTo("https://github.com/testuser/assignment");
    }

    @Test
    void getSubmissionById_ValidId_ReturnsSubmission() {
        when(submissionRepository.findById(1L)).thenReturn(Optional.of(submission));

        SubmissionResponse result = submissionService.getSubmissionById(1L);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
    }

    @Test
    void getSubmissionById_InvalidId_ThrowsException() {
        when(submissionRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> submissionService.getSubmissionById(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Submission not found");
    }
}