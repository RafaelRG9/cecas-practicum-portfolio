package edu.franklin.cecas.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import edu.franklin.cecas.domain.*;
import edu.franklin.cecas.support.MySqlDataJpaTest;

@MySqlDataJpaTest
public class ChairCourseAssignmentRepositoryTest {

    @Autowired
    private ChairCourseAssignmentRepository assignmentRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CourseRepository courseRepository;

    private User createChair() {
        User chair = new User();
        chair.setFullName("Chair User");
        chair.setEmail("chair@test.com");
        chair.setPassword("12345678");
        chair.setRole(UserRole.CHAIR);
        chair.setProgram("CS");
        chair.setIsActive(true);
        chair.setEmailVerified(true);
        chair.setMustChangePassword(false);
        return userRepository.save(chair);
    }

    private Course createCourse() {
        Course course = new Course("COMP110", "26/SU", "001");
        course.setActive(true);
        return courseRepository.save(course);
    }

    @Test
    public void testExistsByChairAndCourse() {

        User chair = createChair();
        Course course = createCourse();

        ChairCourseAssignment assignment =
                new ChairCourseAssignment(chair, course);

        assignmentRepository.save(assignment);

        boolean exists = assignmentRepository
                .existsByChair_IdAndCourse_CourseId(
                        chair.getId(),
                        course.getCourseId()
                );

        assertThat(exists).isTrue();
    }

    @Test
    public void testFindAllByChairId() {

        User chair = createChair();
        Course course1 = createCourse();
        Course course2 = new Course("COMP120", "26/FA", "001");
        course2.setActive(true);
        courseRepository.save(course2);

        assignmentRepository.saveAll(List.of(
                new ChairCourseAssignment(chair, course1),
                new ChairCourseAssignment(chair, course2)
        ));

        List<ChairCourseAssignment> results =
                assignmentRepository.findAllByChairId(chair.getId());

        assertThat(results).hasSize(2);

        assertThat(results)
                .extracting(a -> a.getCourse().getCourseCode())
                .containsExactlyInAnyOrder("COMP110", "COMP120");
    }

    @Test
    public void testDeleteAllByChairId() {

        User chair = createChair();
        Course course = createCourse();

        assignmentRepository.save(
                new ChairCourseAssignment(chair, course)
        );

        assignmentRepository.deleteAllByChairId(chair.getId());

        List<ChairCourseAssignment> results =
                assignmentRepository.findAllByChairId(chair.getId());

        assertThat(results).isEmpty();
    }
}