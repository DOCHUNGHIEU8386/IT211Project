package com.example.it211project.service;

import com.example.it211project.dto.request.SubmissionRequest;
import com.example.it211project.dto.response.SubmissionResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface SubmissionService {

    /** FR-07: Nộp bài bằng link GitHub */
    SubmissionResponse submitByLink(SubmissionRequest request);

    /** FR-07: Nộp bài bằng file upload */
    SubmissionResponse submitByFile(Long studentId, Long courseId, String note, MultipartFile file);

    /** Lấy danh sách bài nộp theo sinh viên */
    List<SubmissionResponse> getSubmissionsByStudent(Long studentId);

    /** Lấy danh sách bài nộp theo khóa học (lecturer/admin dùng) */
    List<SubmissionResponse> getSubmissionsByCourse(Long courseId);

    /** Lấy chi tiết bài nộp theo ID */
    SubmissionResponse getSubmissionById(Long submissionId);
}
