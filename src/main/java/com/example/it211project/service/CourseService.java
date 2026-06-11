package com.example.it211project.service;

import com.example.it211project.dto.request.CourseRequest;
import com.example.it211project.dto.response.CourseResponse;
import org.springframework.data.domain.Page;

public interface CourseService {

    CourseResponse save(CourseRequest request);

    Page<CourseResponse> getCourses(int page, int size, String keyword);

    CourseResponse getCourseById(Long id);

    CourseResponse update(Long id, CourseRequest request);

    void delete(Long id);
}