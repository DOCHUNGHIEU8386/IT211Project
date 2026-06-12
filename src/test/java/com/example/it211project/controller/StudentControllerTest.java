package com.example.it211project.controller;

import com.example.it211project.dto.request.EnrollmentRequest;
import com.example.it211project.dto.response.EnrollmentResponse;
import com.example.it211project.dto.response.GradeResponse;
import com.example.it211project.dto.response.SubmissionResponse;
import com.example.it211project.entity.Submission;
import com.example.it211project.service.EnrollmentService;
import com.example.it211project.service.GradeService;
import com.example.it211project.service.SubmissionService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class StudentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private EnrollmentService enrollmentService;

    @MockitoBean
    private SubmissionService submissionService;

    @MockitoBean
    private GradeService gradeService;

    private String studentToken;
    private Long studentId;
    private EnrollmentRequest enrollmentRequest;
    private EnrollmentResponse enrollmentResponse;
    private SubmissionResponse submissionResponse;
    private GradeResponse gradeResponse;

    @BeforeEach
    void setUp() {
        studentToken = "eyJhbGciOiJIUzI1NiJ9.mock-student-token";
        studentId = 2L;

        // Enrollment data
        enrollmentRequest = EnrollmentRequest.builder()
                .studentId(2L)
                .courseId(1L)
                .build();

        enrollmentResponse = EnrollmentResponse.builder()
                .id(1L)
                .studentId(2L)
                .studentName("Test Student")
                .courseId(1L)
                .courseName("Java Programming")
                .build();

        // Submission data
        submissionResponse = SubmissionResponse.builder()
                .id(1L)
                .studentId(2L)
                .studentName("Test Student")
                .courseId(1L)
                .courseName("Java Programming")
                .githubLink("https://github.com/testuser/repo")
                .submissionType(Submission.SubmissionType.LINK)
                .submittedAt(LocalDateTime.now())
                .build();

        // Grade data
        gradeResponse = GradeResponse.builder()
                .id(1L)
                .submissionId(1L)
                .studentId(2L)
                .studentName("Test Student")
                .courseId(1L)
                .courseName("Java Programming")
                .score(8.5)
                .feedback("Good work!")
                .build();
    }

    // ==================== Test Enroll Course - Success ====================
    @Test
    void enrollCourse_ValidRequest_Returns201() throws Exception {
        when(enrollmentService.enroll(any(EnrollmentRequest.class))).thenReturn(enrollmentResponse);

        mockMvc.perform(post("/api/v1/student/enrollments")
                        .header("Authorization", "Bearer " + studentToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(enrollmentRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Enroll course success"))
                .andExpect(jsonPath("$.data.courseName").value("Java Programming"));
    }

    // ==================== Test Enroll Course - Duplicate ====================
    @Test
    void enrollCourse_Duplicate_Returns409() throws Exception {
        when(enrollmentService.enroll(any(EnrollmentRequest.class)))
                .thenThrow(new RuntimeException("Student already enrolled"));

        mockMvc.perform(post("/api/v1/student/enrollments")
                        .header("Authorization", "Bearer " + studentToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(enrollmentRequest)))
                .andExpect(status().isConflict());
    }

    // ==================== Test Get Enrollments - Success ====================
    @Test
    void getMyEnrollments_ValidStudentId_Returns200() throws Exception {
        when(enrollmentService.getEnrollmentsByStudent(studentId)).thenReturn(List.of(enrollmentResponse));

        mockMvc.perform(get("/api/v1/student/enrollments/{studentId}", studentId)
                        .header("Authorization", "Bearer " + studentToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].courseName").value("Java Programming"));
    }

    // ==================== Test Submit By Link - Success ====================
    @Test
    void submitByLink_ValidRequest_Returns201() throws Exception {
        String requestJson = """
                {
                    "studentId": 2,
                    "courseId": 1,
                    "githubLink": "https://github.com/testuser/repo",
                    "note": "My assignment submission"
                }
                """;

        when(submissionService.submitByLink(any())).thenReturn(submissionResponse);

        mockMvc.perform(post("/api/v1/student/submissions/link")
                        .header("Authorization", "Bearer " + studentToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Submission submitted successfully"));
    }

    // ==================== Test Submit By Link - Invalid GitHub URL ====================
    @Test
    void submitByLink_InvalidGithubUrl_Returns400() throws Exception {
        String requestJson = """
                {
                    "studentId": 2,
                    "courseId": 1,
                    "githubLink": "https://gitlab.com/testuser/repo",
                    "note": "Invalid link"
                }
                """;

        mockMvc.perform(post("/api/v1/student/submissions/link")
                        .header("Authorization", "Bearer " + studentToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isBadRequest());
    }

    // ==================== Test Submit By File - Success ====================
    @Test
    void submitByFile_ValidFile_Returns201() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "assignment.pdf",
                MediaType.APPLICATION_PDF_VALUE,
                "PDF content".getBytes()
        );

        when(submissionService.submitByFile(anyLong(), anyLong(), anyString(), any()))
                .thenReturn(submissionResponse);

        mockMvc.perform(multipart("/api/v1/student/submissions/file")
                        .file(file)
                        .param("studentId", "2")
                        .param("courseId", "1")
                        .param("note", "My file submission")
                        .header("Authorization", "Bearer " + studentToken))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true));
    }

    // ==================== Test Get My Submissions ====================
    @Test
    void getMySubmissions_ValidStudentId_Returns200() throws Exception {
        when(submissionService.getSubmissionsByStudent(studentId)).thenReturn(List.of(submissionResponse));

        mockMvc.perform(get("/api/v1/student/submissions/{studentId}", studentId)
                        .header("Authorization", "Bearer " + studentToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].githubLink").value("https://github.com/testuser/repo"));
    }

    // ==================== Test Get My Grades ====================
    @Test
    void getMyGrades_ValidStudentId_Returns200() throws Exception {
        when(gradeService.getGradesByStudent(studentId)).thenReturn(List.of(gradeResponse));

        mockMvc.perform(get("/api/v1/student/grades/{studentId}", studentId)
                        .header("Authorization", "Bearer " + studentToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].score").value(8.5));
    }
}