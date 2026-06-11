package com.example.it211project.service;

import com.example.it211project.dto.request.EnrollmentRequest;
import com.example.it211project.dto.response.EnrollmentResponse;

import java.util.List;

public interface EnrollmentService {

    EnrollmentResponse enroll(EnrollmentRequest request);

    List<EnrollmentResponse> getEnrollmentsByStudent(Long studentId);
}