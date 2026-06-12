package com.example.it211project.service;

import com.example.it211project.dto.request.GradeRequest;
import com.example.it211project.dto.response.GradeResponse;

import java.util.List;

public interface GradeService {

    /** FR-08: Chấm điểm lần đầu */
    GradeResponse grade(GradeRequest request);

    /** FR-08: Cập nhật điểm / feedback */
    GradeResponse updateGrade(Long gradeId, GradeRequest request);

    /** Lấy điểm theo submission */
    GradeResponse getGradeBySubmission(Long submissionId);

    /** Lấy tất cả điểm của sinh viên */
    List<GradeResponse> getGradesByStudent(Long studentId);

    /** Lấy tất cả điểm trong khóa học */
    List<GradeResponse> getGradesByCourse(Long courseId);
}
