package edu.franklin.cecas.domain;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;

@Entity
@Table(name = "chair_course_assignments")
public class ChairCourseAssignment {

    @EmbeddedId
    private ChairCourseAssignmentId id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @MapsId("chairId") // maps to chair_id in the composite key or the embedded id
    @JoinColumn(name = "chair_id", referencedColumnName = "id", nullable = false)
    private User chair;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @MapsId("courseId") // maps to course_id in the composite key or the embedded id
    @JoinColumn(name = "course_id", referencedColumnName = "course_id", nullable = false)
    private Course course;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    public ChairCourseAssignment() {}

    public ChairCourseAssignment(User chair, Course course) {
        this.chair = chair;
        this.course = course;
        this.id = new ChairCourseAssignmentId(chair.getId(), course.getCourseId());
    }

    public ChairCourseAssignmentId getId() {
        return id;
    }

    public User getChair() {
        return chair;
    }

    public void setChair(User chair) {
        this.chair = chair;
    }

    public Course getCourse() {
        return course;
    }

    public void setCourse(Course course) {
        this.course = course;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}