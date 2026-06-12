package com.example.it211project.service.impl;

import com.example.it211project.dto.response.LectureMaterialResponse;
import com.example.it211project.entity.Course;
import com.example.it211project.entity.LectureMaterial;
import com.example.it211project.entity.User;
import com.example.it211project.exception.ResourceNotFoundException;
import com.example.it211project.repository.CourseRepository;
import com.example.it211project.repository.LectureMaterialRepository;
import com.example.it211project.repository.UserRepository;
import com.example.it211project.service.LectureMaterialService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LectureMaterialServiceImpl implements LectureMaterialService {

    private final LectureMaterialRepository materialRepository;
    private final CourseRepository courseRepository;
    private final UserRepository userRepository;

    @Value("${upload.material-dir:uploads/materials}")
    private String materialDir;

    // ==================== FR-09: Upload tài liệu bài giảng ====================
    @Override
    public LectureMaterialResponse uploadMaterial(Long courseId, Long lecturerId,
                                                   String title, String description,
                                                   MultipartFile file) {
        // Validate file
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File is required");
        }
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("Title is required");
        }

        long maxSize = 100 * 1024 * 1024L; // 100MB
        if (file.getSize() > maxSize) {
            throw new IllegalArgumentException("File size must not exceed 100MB");
        }

        String originalName = file.getOriginalFilename();
        if (originalName == null) {
            throw new IllegalArgumentException("Invalid file name");
        }

        // Cho phép các định dạng tài liệu phổ biến
        String extension = getExtension(originalName);
        List<String> allowed = List.of(".pdf", ".pptx", ".ppt", ".docx", ".doc",
                ".xlsx", ".xls", ".mp4", ".zip");
        if (!allowed.contains(extension.toLowerCase())) {
            throw new IllegalArgumentException(
                    "File type not allowed. Allowed: " + String.join(", ", allowed));
        }

        Course course = courseRepository.findById(courseId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Course not found with id: " + courseId));

        User lecturer = userRepository.findById(lecturerId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Lecturer not found with id: " + lecturerId));

        // Lưu file
        String storedFileName = UUID.randomUUID() + extension;
        Path uploadPath = Paths.get(materialDir);
        try {
            Files.createDirectories(uploadPath);
            Files.copy(file.getInputStream(),
                    uploadPath.resolve(storedFileName),
                    StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new RuntimeException("Failed to store material: " + e.getMessage());
        }

        LectureMaterial material = LectureMaterial.builder()
                .course(course)
                .lecturer(lecturer)
                .title(title)
                .description(description)
                .fileName(originalName)
                .filePath(materialDir + "/" + storedFileName)
                .fileType(extension)
                .fileSize(file.getSize())
                .uploadedAt(LocalDateTime.now())
                .build();

        return mapToResponse(materialRepository.save(material));
    }

    @Override
    public List<LectureMaterialResponse> getMaterialsByCourse(Long courseId) {
        if (!courseRepository.existsById(courseId)) {
            throw new ResourceNotFoundException("Course not found with id: " + courseId);
        }
        return materialRepository.findByCourseId(courseId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public LectureMaterialResponse getMaterialById(Long materialId) {
        return mapToResponse(materialRepository.findById(materialId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Material not found with id: " + materialId)));
    }

    @Override
    public void deleteMaterial(Long materialId) {
        LectureMaterial material = materialRepository.findById(materialId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Material not found with id: " + materialId));
        // Xóa file vật lý
        if (material.getFilePath() != null) {
            try {
                Files.deleteIfExists(Paths.get(material.getFilePath()));
            } catch (IOException ignored) { }
        }
        materialRepository.delete(material);
    }

    private String getExtension(String fileName) {
        int idx = fileName.lastIndexOf('.');
        return idx >= 0 ? fileName.substring(idx) : "";
    }

    private LectureMaterialResponse mapToResponse(LectureMaterial m) {
        return LectureMaterialResponse.builder()
                .id(m.getId())
                .courseId(m.getCourse().getId())
                .courseName(m.getCourse().getCourseName())
                .lecturerId(m.getLecturer().getId())
                .lecturerName(m.getLecturer().getFullName())
                .title(m.getTitle())
                .description(m.getDescription())
                .fileName(m.getFileName())
                .fileType(m.getFileType())
                .fileSize(m.getFileSize())
                .uploadedAt(m.getUploadedAt())
                .build();
    }
}
