package edu.franklin.cecas.repository;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import edu.franklin.cecas.domain.ChairCourseAssignment;
import edu.franklin.cecas.domain.ChairCourseAssignmentId;

@Repository
public interface ChairCourseAssignmentRepository extends JpaRepository<ChairCourseAssignment, ChairCourseAssignmentId> {
    boolean existsByChair_IdAndCourse_CourseId(Integer chairId, Integer courseId);

    List<ChairCourseAssignment> findAllByChairId(Integer chairId);

    void deleteAllByChairId(Integer chairId);
}
