package com.example.it211project.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "courses")
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class Course {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String courseName;

    private String description;

    private Boolean status;
}