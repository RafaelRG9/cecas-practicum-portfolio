package edu.franklin.cecas.repository;

import java.util.List;
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
        course.setCourseCode("COMP-110");
        course.setTerm("26/SU");
        course.setSection("001");
        course.setActive(true);
        return course;
    }

    private List<Course> createCoursesList() {
        Course course1 = new Course();
        course1.setCourseCode("COMP-110");
        course1.setTerm("26/SU");
        course1.setSection("001");
        course1.setActive(true);
        Course course2 = new Course();
        course2.setCourseCode("COMP-110");
        course2.setTerm("26/FA");
        course2.setSection("001");
        course2.setActive(true);
        Course course3 = new Course();
        course3.setCourseCode("COMP-110");
        course3.setTerm("26/SP");
        course3.setSection("001");
        course3.setActive(false);
        return List.of(course1, course2, course3);
    }

    @Test
    public void testFindByCourseCodeAndTermAndSection() {
        Course course = createTestCourse();
        courseRepository.save(course);

        Optional<Course> result = courseRepository.findByCourseCodeAndTermAndSection("COMP-110", "26/su", "001");
        assertThat(result).isPresent();
        assertThat(result.get().getCourseCode()).isEqualTo("COMP-110");
        assertThat(result.get().getTerm()).isEqualTo("26/SU"); // case-insensitive match
        assertThat(result.get().getSection()).isEqualTo("001");
        assertThat(result.get().isActive()).isTrue();
    }

@Test
public void testFindAllByCourseCodeAndIsActiveTrue() {

    List<Course> courses = createCoursesList();
    courseRepository.saveAll(courses);

    List<Course> results =
        courseRepository.findAllByCourseCodeAndIsActiveTrue("COMP-110");

    assertThat(results).hasSize(2);
    assertThat(results)
        .extracting(Course::getTerm)
        .containsExactlyInAnyOrder("26/SU", "26/FA");
    assertThat(results)
        .allMatch(Course::isActive);
}
@Test
public void testFindAllByIsActiveTrue() {

    List<Course> courses = createCoursesList();
    courseRepository.saveAll(courses);

    List<Course> results = courseRepository.findAllByIsActiveTrue();

    assertThat(results).hasSize(2);
    assertThat(results)
        .allMatch(Course::isActive);
}
}
