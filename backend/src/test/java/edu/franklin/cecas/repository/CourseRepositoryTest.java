package edu.franklin.cecas.repository;

import java.util.Optional;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import edu.franklin.cecas.domain.Course;
import edu.franklin.cecas.support.MySqlDataJpaTest;

@MySqlDataJpaTest
public class CourseRepositoryTest {

    @Autowired
    private CourseRepository courseRepository;

    private Course createTestCourse() {
        Course course = new Course();
        course.setCourseCode("COMP325");
        course.setTerm("Fall 2024");
        course.setSection("001");
        return course;
    }

    @Test
    public void testFindByCourseCode() {
        Course course = createTestCourse();
        courseRepository.save(course);

        Optional<Course> result = courseRepository.findByCourseCode("COMP325");

        assertThat(result).isPresent();
        assertThat(result.get().getCourseCode()).isEqualTo("COMP325");
        assertThat(result.get().getTerm()).isEqualTo("Fall 2024");
        assertThat(result.get().getSection()).isEqualTo("001");
    }
}
