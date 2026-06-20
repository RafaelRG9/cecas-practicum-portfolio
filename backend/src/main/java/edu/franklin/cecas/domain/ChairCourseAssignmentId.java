package edu.franklin.cecas.domain;

import java.io.Serializable;
import java.util.Objects;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

/**
 * Composite primary key class for ChairCourseAssignment entity.
 * Combines chairId and courseId to uniquely identify each assignment.
 */
@Embeddable
public class ChairCourseAssignmentId implements Serializable {

    @Column(name = "chair_id")
    private Integer chairId;

    @Column(name = "course_id")
    private Integer courseId;

    public ChairCourseAssignmentId() {
    }

    public ChairCourseAssignmentId(Integer chairId, Integer courseId) {
        this.chairId = chairId;
        this.courseId = courseId;
    }

    public Integer getChairId() {
        return chairId;
    }

    public Integer getCourseId() {
        return courseId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ChairCourseAssignmentId that)) return false;
        return Objects.equals(chairId, that.chairId)
                && Objects.equals(courseId, that.courseId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(chairId, courseId);
    }
}