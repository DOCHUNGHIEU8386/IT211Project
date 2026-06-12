package com.example.it211project.service;

import com.example.it211project.dto.response.LectureMaterialResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface LectureMaterialService {

    /** FR-09: Giảng viên upload tài liệu bài giảng */
    LectureMaterialResponse uploadMaterial(Long courseId, Long lecturerId,
                                           String title, String description,
                                           MultipartFile file);

    /** Lấy danh sách tài liệu theo khóa học */
    List<LectureMaterialResponse> getMaterialsByCourse(Long courseId);

    /** Lấy tài liệu theo ID */
    LectureMaterialResponse getMaterialById(Long materialId);

    /** Xóa tài liệu */
    void deleteMaterial(Long materialId);
}
