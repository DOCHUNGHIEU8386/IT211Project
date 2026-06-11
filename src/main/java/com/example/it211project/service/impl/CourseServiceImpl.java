package com.example.it211project.service.impl;

import com.example.it211project.dto.request.CourseRequest;
import com.example.it211project.dto.response.CourseResponse;
import com.example.it211project.entity.Course;
import com.example.it211project.exception.ResourceNotFoundException;
import com.example.it211project.repository.CourseRepository;
import com.example.it211project.service.CourseService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CourseServiceImpl implements CourseService {

    private final CourseRepository courseRepository;

    // ==================== FR-05: Create course ====================
    @Override
    public CourseResponse save(CourseRequest request) {
        Course course = Course.builder()
                .courseName(request.getCourseName())
                .description(request.getDescription())
                .status(request.getStatus() != null ? request.getStatus() : true)
                .build();

        return mapToResponse(courseRepository.save(course));
    }

    // ==================== FR-05: Get courses (paginated + search) ====================
    @Override
    public Page<CourseResponse> getCourses(int page, int size, String keyword) {
        PageRequest pageable = PageRequest.of(page, size);

        if (keyword != null && !keyword.isBlank()) {
            return courseRepository
                    .findByCourseNameContainingIgnoreCase(keyword, pageable)
                    .map(this::mapToResponse);
        }

        return courseRepository.findAll(pageable).map(this::mapToResponse);
    }

    // ==================== FR-05: Get course by ID ====================
    @Override
    public CourseResponse getCourseById(Long id) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Course not found with id: " + id));
        return mapToResponse(course);
    }

    // ==================== FR-05: Update course ====================
    @Override
    public CourseResponse update(Long id, CourseRequest request) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Course not found with id: " + id));

        course.setCourseName(request.getCourseName());
        course.setDescription(request.getDescription());
        if (request.getStatus() != null) {
            course.setStatus(request.getStatus());
        }

        return mapToResponse(courseRepository.save(course));
    }

    // ==================== FR-05: Delete course ====================
    @Override
    public void delete(Long id) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Course not found with id: " + id));
        courseRepository.delete(course);
    }

    // ==================== Helper: Entity -> DTO ====================
    private CourseResponse mapToResponse(Course course) {
        return CourseResponse.builder()
                .id(course.getId())
                .courseName(course.getCourseName())
                .description(course.getDescription())
                .status(course.getStatus())
                .build();
    }
}