package edu.franklin.cecas.domain;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name = "courses",
    uniqueConstraints = {
        @UniqueConstraint(name = "course_code_term_section_IDX", columnNames = {"course_code", "term", "section"})
    }
)

public class Course {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "course_id", nullable = false)
    private Integer courseId;

    @Column(name = "course_code", length = 45, nullable = false)
    private String courseCode;

    @Column(name = "term", length = 45, nullable = false)
    private String term;

    @Column(name = "section", length = 45, nullable = false)
    private String section;

    @Column(name = "is_active", nullable = false)
    private boolean isActive = true;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    public Course() {}

    // this constructor sets composite key fields and defaults isActive to true
    public Course(String courseCode, String term, String section) {
        this.courseCode = courseCode;
        this.term = term;
        this.section = section;
        this.isActive = true;
    }

    public Integer getCourseId() {
        return courseId;
    }

    public String getCourseCode() {
        return courseCode;
    }

    public void setCourseCode(String courseCode) {
        this.courseCode = courseCode;
    }

    public String getTerm() {
        return term;
    }

    public void setTerm(String term) {
        this.term = term;
    }

    public String getSection() {
        return section;
    }

    public void setSection(String section) {
        this.section = section;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean isActive) {
        this.isActive = isActive;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}