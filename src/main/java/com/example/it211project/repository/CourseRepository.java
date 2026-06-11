    package com.example.it211project.repository;

    import com.example.it211project.entity.Course;
    import org.springframework.data.domain.Page;
    import org.springframework.data.domain.Pageable;
    import org.springframework.data.jpa.repository.JpaRepository;

    public interface CourseRepository extends JpaRepository<Course, Long> {

        Page<Course> findByCourseNameContainingIgnoreCase(String courseName, Pageable pageable);

        Page<Course> findAll(Pageable pageable);
    }