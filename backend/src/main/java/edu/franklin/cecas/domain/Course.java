package edu.franklin.cecas.domain;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "courses")

public class Course {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "course_id", nullable = false)
    private Integer courseId;

    @Column(name = "course_code", nullable = false)
    private String courseCode;

    @Column(name = "term")
    private String term;

    @Column(name = "section")
    private String section;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;
}