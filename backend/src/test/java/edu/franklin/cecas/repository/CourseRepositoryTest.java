package edu.franklin.cecas.repository;

import java.util.Optional;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;

import edu.franklin.cecas.domain.Course;
import edu.franklin.cecas.support.MySqlTestcontainers;
@DataJpaTest
@Import(MySqlTestcontainers.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class CourseRepositoryTest {

    @Autowired
    private CourseRepository courseRepository;

    private Course createTestCourse() {
        Course course = new Course();
        course.setCourseCode("COMP325");
        return course;

    }

    @Test
    public void testFindByCourseCode() {
        Course course = createTestCourse();
        courseRepository.save(course);

        Optional<Course> result = courseRepository.findByCourseCode("COMP325");

        assertThat(result).isPresent();
        assertThat(result.get().getCourseCode()).isEqualTo("COMP325");
    }
}