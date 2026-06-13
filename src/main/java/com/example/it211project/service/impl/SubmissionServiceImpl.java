package com.example.it211project.service.impl;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
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
import com.example.it211project.service.SubmissionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class SubmissionServiceImpl implements SubmissionService {

    private final SubmissionRepository submissionRepository;
    private final UserRepository userRepository;
    private final CourseRepository courseRepository;
    private final Cloudinary cloudinary;

    @Value("${upload.submission-dir:uploads/submissions}")
    private String submissionDir;

    // ==================== FR-07: Nộp bài bằng GitHub link ====================
    @Override
    public SubmissionResponse submitByLink(SubmissionRequest request) {
        if (request.getGithubLink() == null || request.getGithubLink().isBlank()) {
            throw new IllegalArgumentException("GitHub link is required for LINK submission");
        }
        if (!request.getGithubLink().startsWith("https://github.com/")) {
            throw new IllegalArgumentException("GitHub link must start with https://github.com/");
        }

        User student = getStudent(request.getStudentId());
        Course course = getCourse(request.getCourseId());
        checkDuplicate(request.getStudentId(), request.getCourseId());

        Submission submission = Submission.builder()
                .student(student)
                .course(course)
                .githubLink(request.getGithubLink())
                .note(request.getNote())
                .submissionType(Submission.SubmissionType.LINK)
                .submittedAt(LocalDateTime.now())
                .build();

        return mapToResponse(submissionRepository.save(submission));
    }

    // ==================== FR-07 NÂNG CAO: Nộp bài bằng file upload lên Cloudinary ====================
    @Override
    public SubmissionResponse submitByFile(Long studentId, Long courseId,
                                           String note, MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File is required for FILE submission");
        }

        long maxSize = 50 * 1024 * 1024L;
        if (file.getSize() > maxSize) {
            throw new IllegalArgumentException("File size must not exceed 50MB");
        }

        String originalName = file.getOriginalFilename();
        if (originalName == null) {
            throw new IllegalArgumentException("Invalid file name");
        }

        String extension = getExtension(originalName);
        List<String> allowedExtensions = List.of(".zip", ".rar", ".pdf", ".docx", ".pptx", ".java", ".py");
        if (!allowedExtensions.contains(extension.toLowerCase())) {
            throw new IllegalArgumentException(
                    "File type not allowed. Allowed: " + String.join(", ", allowedExtensions));
        }

        User student = getStudent(studentId);
        Course course = getCourse(courseId);
        checkDuplicate(studentId, courseId);

        String cloudinaryUrl = null;

        // FR-07 NÂNG CAO: Upload file lên Cloudinary nếu đã cấu hình
        if (cloudinary != null) {
            try {
                Map<?, ?> uploadResult = cloudinary.uploader().upload(file.getBytes(), ObjectUtils.asMap(
                        "folder", "it211project/submissions",
                        "resource_type", "auto",
                        "public_id", UUID.randomUUID().toString()
                ));

                cloudinaryUrl = uploadResult.get("secure_url").toString();
                log.info("File uploaded to Cloudinary: {}", cloudinaryUrl);

            } catch (IOException e) {
                log.error("Failed to upload file to Cloudinary: {}", e.getMessage());
                throw new RuntimeException("Failed to upload file to Cloudinary: " + e.getMessage());
            }
        } else {
            // Fallback: Lưu local nếu chưa cấu hình Cloudinary
            log.warn("Cloudinary not configured, saving file locally");
            cloudinaryUrl = saveFileLocally(file, extension);
        }

        Submission submission = Submission.builder()
                .student(student)
                .course(course)
                .fileName(originalName)
                .filePath(cloudinaryUrl)
                .note(note)
                .submissionType(Submission.SubmissionType.FILE)
                .submittedAt(LocalDateTime.now())
                .build();

        return mapToResponse(submissionRepository.save(submission));
    }

    /**
     * Lưu file local (fallback khi không có Cloudinary)
     */
    private String saveFileLocally(MultipartFile file, String extension) {
        try {
            String storedFileName = UUID.randomUUID() + extension;
            java.nio.file.Path uploadPath = java.nio.file.Paths.get(submissionDir);
            java.nio.file.Files.createDirectories(uploadPath);
            java.nio.file.Files.copy(file.getInputStream(),
                    uploadPath.resolve(storedFileName),
                    java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            return submissionDir + "/" + storedFileName;
        } catch (IOException e) {
            throw new RuntimeException("Failed to store file: " + e.getMessage());
        }
    }

    @Override
    public List<SubmissionResponse> getSubmissionsByStudent(Long studentId) {
        if (!userRepository.existsById(studentId)) {
            throw new ResourceNotFoundException("Student not found with id: " + studentId);
        }
        return submissionRepository.findByStudentId(studentId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<SubmissionResponse> getSubmissionsByCourse(Long courseId) {
        if (!courseRepository.existsById(courseId)) {
            throw new ResourceNotFoundException("Course not found with id: " + courseId);
        }
        return submissionRepository.findByCourseId(courseId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public SubmissionResponse getSubmissionById(Long submissionId) {
        return mapToResponse(submissionRepository.findById(submissionId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Submission not found with id: " + submissionId)));
    }

    // ==================== Helper methods ====================

    private User getStudent(Long studentId) {
        return userRepository.findById(studentId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Student not found with id: " + studentId));
    }

    private Course getCourse(Long courseId) {
        return courseRepository.findById(courseId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Course not found with id: " + courseId));
    }

    private void checkDuplicate(Long studentId, Long courseId) {
        if (submissionRepository.existsByStudentIdAndCourseId(studentId, courseId)) {
            throw new DuplicateResourceException(
                    "Student has already submitted for this course");
        }
    }

    private String getExtension(String fileName) {
        int idx = fileName.lastIndexOf('.');
        return idx >= 0 ? fileName.substring(idx) : "";
    }

    private SubmissionResponse mapToResponse(Submission s) {
        return SubmissionResponse.builder()
                .id(s.getId())
                .studentId(s.getStudent().getId())
                .studentName(s.getStudent().getFullName())
                .courseId(s.getCourse().getId())
                .courseName(s.getCourse().getCourseName())
                .githubLink(s.getGithubLink())
                .fileName(s.getFileName())
                .submissionType(s.getSubmissionType())
                .note(s.getNote())
                .submittedAt(s.getSubmittedAt())
                .build();
    }
}