package com.example.it211project.service;

import com.example.it211project.dto.request.CourseRequest;
import com.example.it211project.dto.response.CourseResponse;
import com.example.it211project.entity.Course;
import com.example.it211project.exception.ResourceNotFoundException;
import com.example.it211project.repository.CourseRepository;
import com.example.it211project.service.impl.CourseServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CourseServiceTest {

    @Mock
    private CourseRepository courseRepository;

    @InjectMocks
    private CourseServiceImpl courseService;

    private CourseRequest courseRequest;
    private Course course;
    private CourseResponse courseResponse;

    @BeforeEach
    void setUp() {
        courseRequest = CourseRequest.builder()
                .courseName("Java Programming")
                .description("Learn Java from scratch")
                .status(true)
                .build();

        course = Course.builder()
                .id(1L)
                .courseName("Java Programming")
                .description("Learn Java from scratch")
                .status(true)
                .build();

        courseResponse = CourseResponse.builder()
                .id(1L)
                .courseName("Java Programming")
                .description("Learn Java from scratch")
                .status(true)
                .build();
    }

    // ==================== Test Save Course - Success ====================
    @Test
    void save_ValidRequest_ReturnsCourseResponse() {
        when(courseRepository.save(any(Course.class))).thenReturn(course);

        CourseResponse result = courseService.save(courseRequest);

        assertThat(result).isNotNull();
        assertThat(result.getCourseName()).isEqualTo("Java Programming");
        assertThat(result.getStatus()).isTrue();

        verify(courseRepository).save(any(Course.class));
    }

    // ==================== Test Save Course - With Null Status (Default True) ====================
    @Test
    void save_NullStatus_DefaultsToTrue() {
        CourseRequest requestWithNullStatus = CourseRequest.builder()
                .courseName("Python")
                .description("Python course")
                .status(null)
                .build();

        Course courseWithDefaultStatus = Course.builder()
                .id(2L)
                .courseName("Python")
                .description("Python course")
                .status(true)
                .build();

        when(courseRepository.save(any(Course.class))).thenReturn(courseWithDefaultStatus);

        CourseResponse result = courseService.save(requestWithNullStatus);

        assertThat(result.getStatus()).isTrue();
    }

    // ==================== Test Get Course By ID - Success ====================
    @Test
    void getCourseById_ValidId_ReturnsCourseResponse() {
        when(courseRepository.findById(1L)).thenReturn(Optional.of(course));

        CourseResponse result = courseService.getCourseById(1L);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getCourseName()).isEqualTo("Java Programming");

        verify(courseRepository).findById(1L);
    }

    // ==================== Test Get Course By ID - Not Found ====================
    @Test
    void getCourseById_InvalidId_ThrowsException() {
        when(courseRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> courseService.getCourseById(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Course not found with id: 999");
    }

    // ==================== Test Get Courses - Without Keyword ====================
    @Test
    void getCourses_NoKeyword_ReturnsPage() {
        PageRequest pageable = PageRequest.of(0, 10);
        Page<Course> coursePage = new PageImpl<>(List.of(course));

        when(courseRepository.findAll(pageable)).thenReturn(coursePage);

        Page<CourseResponse> result = courseService.getCourses(0, 10, null);

        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getCourseName()).isEqualTo("Java Programming");
    }

    // ==================== Test Get Courses - With Keyword ====================
    @Test
    void getCourses_WithKeyword_ReturnsFilteredPage() {
        PageRequest pageable = PageRequest.of(0, 10);
        Page<Course> coursePage = new PageImpl<>(List.of(course));

        when(courseRepository.findByCourseNameContainingIgnoreCase(eq("Java"), eq(pageable)))
                .thenReturn(coursePage);

        Page<CourseResponse> result = courseService.getCourses(0, 10, "Java");

        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);

        verify(courseRepository).findByCourseNameContainingIgnoreCase(eq("Java"), any(PageRequest.class));
    }

    // ==================== Test Update Course - Success ====================
    @Test
    void updateCourse_ValidRequest_ReturnsUpdatedCourse() {
        CourseRequest updateRequest = CourseRequest.builder()
                .courseName("Advanced Java")
                .description("Advanced Java concepts")
                .status(true)
                .build();

        Course updatedCourse = Course.builder()
                .id(1L)
                .courseName("Advanced Java")
                .description("Advanced Java concepts")
                .status(true)
                .build();

        when(courseRepository.findById(1L)).thenReturn(Optional.of(course));
        when(courseRepository.save(any(Course.class))).thenReturn(updatedCourse);

        CourseResponse result = courseService.update(1L, updateRequest);

        assertThat(result).isNotNull();
        assertThat(result.getCourseName()).isEqualTo("Advanced Java");
    }

    // ==================== Test Update Course - Not Found ====================
    @Test
    void updateCourse_InvalidId_ThrowsException() {
        when(courseRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> courseService.update(999L, courseRequest))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Course not found with id: 999");
    }

    // ==================== Test Delete Course - Success ====================
    @Test
    void deleteCourse_ValidId_DeletesCourse() {
        when(courseRepository.findById(1L)).thenReturn(Optional.of(course));
        doNothing().when(courseRepository).delete(course);

        courseService.delete(1L);

        verify(courseRepository).delete(course);
    }

    // ==================== Test Delete Course - Not Found ====================
    @Test
    void deleteCourse_InvalidId_ThrowsException() {
        when(courseRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> courseService.delete(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Course not found with id: 999");

        verify(courseRepository, never()).delete(any());
    }
}