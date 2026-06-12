package com.example.it211project.controller;

import com.example.it211project.dto.request.GradeRequest;
import com.example.it211project.dto.response.GradeResponse;
import com.example.it211project.dto.response.LectureMaterialResponse;
import com.example.it211project.dto.response.SubmissionResponse;
import com.example.it211project.entity.Submission;
import com.example.it211project.service.GradeService;
import com.example.it211project.service.LectureMaterialService;
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
public class LecturerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private GradeService gradeService;

    @MockitoBean
    private SubmissionService submissionService;

    @MockitoBean
    private LectureMaterialService lectureMaterialService;

    private String lecturerToken;
    private GradeRequest gradeRequest;
    private GradeResponse gradeResponse;
    private SubmissionResponse submissionResponse;
    private LectureMaterialResponse materialResponse;

    @BeforeEach
    void setUp() {
        lecturerToken = "eyJhbGciOiJIUzI1NiJ9.mock-lecturer-token";

        gradeRequest = GradeRequest.builder()
                .submissionId(1L)
                .lecturerId(3L)
                .score(8.5)
                .feedback("Good work, keep it up!")
                .build();

        gradeResponse = GradeResponse.builder()
                .id(1L)
                .submissionId(1L)
                .studentId(2L)
                .studentName("Test Student")
                .courseId(1L)
                .courseName("Java Programming")
                .lecturerId(3L)
                .lecturerName("Test Lecturer")
                .score(8.5)
                .feedback("Good work, keep it up!")
                .gradedAt(LocalDateTime.now())
                .build();

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

        materialResponse = LectureMaterialResponse.builder()
                .id(1L)
                .courseId(1L)
                .courseName("Java Programming")
                .lecturerId(3L)
                .lecturerName("Test Lecturer")
                .title("Lecture 1: Introduction")
                .fileName("intro.pdf")
                .fileType(".pdf")
                .fileSize(1024L)
                .uploadedAt(LocalDateTime.now())
                .build();
    }

    // ==================== Test Grade Submission - Success ====================
    @Test
    void gradeSubmission_ValidRequest_Returns201() throws Exception {
        when(gradeService.grade(any(GradeRequest.class))).thenReturn(gradeResponse);

        mockMvc.perform(post("/api/v1/lecturer/grades")
                        .header("Authorization", "Bearer " + lecturerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(gradeRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Grade submitted successfully"))
                .andExpect(jsonPath("$.data.score").value(8.5));
    }

    // ==================== Test Grade Submission - Already Graded ====================
    @Test
    void gradeSubmission_AlreadyGraded_Returns409() throws Exception {
        when(gradeService.grade(any(GradeRequest.class)))
                .thenThrow(new RuntimeException("Submission already graded"));

        mockMvc.perform(post("/api/v1/lecturer/grades")
                        .header("Authorization", "Bearer " + lecturerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(gradeRequest)))
                .andExpect(status().isConflict());
    }

    // ==================== Test Update Grade - Success ====================
    @Test
    void updateGrade_ValidRequest_Returns200() throws Exception {
        when(gradeService.updateGrade(eq(1L), any(GradeRequest.class))).thenReturn(gradeResponse);

        mockMvc.perform(put("/api/v1/lecturer/grades/{gradeId}", 1L)
                        .header("Authorization", "Bearer " + lecturerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(gradeRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Grade updated successfully"));
    }

    // ==================== Test Get Grades By Course ====================
    @Test
    void getGradesByCourse_ValidCourseId_Returns200() throws Exception {
        when(gradeService.getGradesByCourse(1L)).thenReturn(List.of(gradeResponse));

        mockMvc.perform(get("/api/v1/lecturer/grades/course/{courseId}", 1L)
                        .header("Authorization", "Bearer " + lecturerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].score").value(8.5));
    }

    // ==================== Test Get Submissions By Course ====================
    @Test
    void getSubmissionsByCourse_ValidCourseId_Returns200() throws Exception {
        when(submissionService.getSubmissionsByCourse(1L)).thenReturn(List.of(submissionResponse));

        mockMvc.perform(get("/api/v1/lecturer/submissions/course/{courseId}", 1L)
                        .header("Authorization", "Bearer " + lecturerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].githubLink").value("https://github.com/testuser/repo"));
    }

    // ==================== Test Upload Material - Success ====================
    @Test
    void uploadMaterial_ValidFile_Returns201() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "lecture.pdf",
                MediaType.APPLICATION_PDF_VALUE,
                "PDF content".getBytes()
        );

        when(lectureMaterialService.uploadMaterial(anyLong(), anyLong(), anyString(), anyString(), any()))
                .thenReturn(materialResponse);

        mockMvc.perform(multipart("/api/v1/lecturer/materials")
                        .file(file)
                        .param("courseId", "1")
                        .param("lecturerId", "3")
                        .param("title", "Lecture 1")
                        .param("description", "Introduction to Java")
                        .header("Authorization", "Bearer " + lecturerToken))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Material uploaded successfully"))
                .andExpect(jsonPath("$.data.title").value("Lecture 1: Introduction"));
    }

    // ==================== Test Upload Material - Invalid File Type ====================
    @Test
    void uploadMaterial_InvalidFileType_Returns400() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "lecture.exe",
                "application/octet-stream",
                "Executable content".getBytes()
        );

        mockMvc.perform(multipart("/api/v1/lecturer/materials")
                        .file(file)
                        .param("courseId", "1")
                        .param("lecturerId", "3")
                        .param("title", "Lecture 1")
                        .header("Authorization", "Bearer " + lecturerToken))
                .andExpect(status().isBadRequest());
    }

    // ==================== Test Get Materials By Course ====================
    @Test
    void getMaterialsByCourse_ValidCourseId_Returns200() throws Exception {
        when(lectureMaterialService.getMaterialsByCourse(1L)).thenReturn(List.of(materialResponse));

        mockMvc.perform(get("/api/v1/lecturer/materials/course/{courseId}", 1L)
                        .header("Authorization", "Bearer " + lecturerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].fileName").value("intro.pdf"));
    }

    // ==================== Test Delete Material - Success ====================
    @Test
    void deleteMaterial_ValidId_Returns200() throws Exception {
        doNothing().when(lectureMaterialService).deleteMaterial(1L);

        mockMvc.perform(delete("/api/v1/lecturer/materials/{materialId}", 1L)
                        .header("Authorization", "Bearer " + lecturerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Material deleted successfully"));
    }
}