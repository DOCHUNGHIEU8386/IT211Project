package com.example.it211project.repository;

import com.example.it211project.entity.LectureMaterial;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LectureMaterialRepository extends JpaRepository<LectureMaterial, Long> {

    List<LectureMaterial> findByCourseId(Long courseId);

    List<LectureMaterial> findByLecturerId(Long lecturerId);
}
