package edu.franklin.cecas.seed;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;

import edu.franklin.cecas.domain.Course;
import edu.franklin.cecas.repository.CourseRepository;
import edu.franklin.cecas.support.MySqlDataJpaTest;

@MySqlDataJpaTest
@Import(CourseSeedImporter.class)
public class CourseSeedImporterTest {

    @Autowired
    private CourseSeedImporter importer;

    @Autowired
    private CourseRepository courseRepository;

    private Course saveCourse(String courseCode, String term, String section, boolean active) {
        Course course = new Course(courseCode, term, section);
        course.setActive(active);
        return courseRepository.save(course);
    }

    /**
     * Verifies that a new course is inserted into the database
     */
    @Test
    void testImportCoursesInsertsNewCourse() {
        CourseSeedImportResult result = importer.importCourses(List.of(
                new CourseSeedRow("COMP-495", "26/FA", "H1WW")));

        List<Course> courses = courseRepository.findAll();

        assertThat(courses).hasSize(1);

        Course savedCourse = courses.get(0);
        assertThat(savedCourse.getCourseCode()).isEqualTo("COMP-495");
        assertThat(savedCourse.getTerm()).isEqualTo("26/FA");
        assertThat(savedCourse.getSection()).isEqualTo("H1WW");
        assertThat(savedCourse.isActive()).isTrue();
        assertThat(savedCourse.getCourseId()).isNotNull();

        assertThat(result).isEqualTo(new CourseSeedImportResult(1, 0, 0, 0));
    }

    /**
     * Verifies that importing an already active course leaves it unchanged.
     */
    @Test
    void testImportCoursesLeavesExistingCourseUnchanged() {
        Course existingCourse = saveCourse("COMP-495", "26/SP", "H2WW", true);

        CourseSeedImportResult result = importer.importCourses(List.of(
                new CourseSeedRow("COMP-495", "26/SP", "H2WW")));

        List<Course> courses = courseRepository.findAll();

        assertThat(courses).hasSize(1);

        Course savedCourse = courses.get(0);
        assertThat(savedCourse.getCourseId()).isEqualTo(existingCourse.getCourseId());
        assertThat(savedCourse.getCourseCode()).isEqualTo("COMP-495");
        assertThat(savedCourse.getTerm()).isEqualTo("26/SP");
        assertThat(savedCourse.getSection()).isEqualTo("H2WW");
        assertThat(savedCourse.isActive()).isTrue();

        assertThat(result).isEqualTo(new CourseSeedImportResult(0, 1, 0, 0));
    }

    /**
     * Verifies that an existing inactive course is reactivated with import.
     */
    @Test
    void testImportCoursesReactivatesInactiveCourse() {
        Course existingCourse = saveCourse("COMP-495", "26/SP", "H2WW", false);

        CourseSeedImportResult result = importer.importCourses(List.of(
                new CourseSeedRow("COMP-495", "26/SP", "H2WW")));

        List<Course> courses = courseRepository.findAll();

        assertThat(courses).hasSize(1);

        Course savedCourse = courses.get(0);
        assertThat(savedCourse.getCourseId()).isEqualTo(existingCourse.getCourseId());
        assertThat(savedCourse.getCourseCode()).isEqualTo("COMP-495");
        assertThat(savedCourse.getTerm()).isEqualTo("26/SP");
        assertThat(savedCourse.getSection()).isEqualTo("H2WW");
        assertThat(savedCourse.isActive()).isTrue();

        assertThat(result).isEqualTo(new CourseSeedImportResult(0, 0, 1, 0));
    }

    /**
     * Verifies an existing active course is deactivated if missing on import.
     */
    @Test
    void testImportCoursesDeactivatesActiveCourseWhenMissing() {
        Course existingCourse = saveCourse("COMP-495", "26/SP", "H2WW", true);

        CourseSeedImportResult result = importer.importCourses(List.of());

        List<Course> courses = courseRepository.findAll();

        assertThat(courses).hasSize(1);

        Course savedCourse = courses.get(0);
        assertThat(savedCourse.getCourseId()).isEqualTo(existingCourse.getCourseId());
        assertThat(savedCourse.getCourseCode()).isEqualTo("COMP-495");
        assertThat(savedCourse.getTerm()).isEqualTo("26/SP");
        assertThat(savedCourse.getSection()).isEqualTo("H2WW");
        assertThat(savedCourse.isActive()).isFalse();

        assertThat(result).isEqualTo(new CourseSeedImportResult(0, 0, 0, 1));
    }

    /**
     * Verifies that changing any part of the course natural key creates a new
     * course record and deactivates the previously active one.
     */
    @Test
    void testImportCoursesTreatsNaturalKeyChangeAsInsertAndDeactivate() {
        Course oldCourse = saveCourse("COMP-495", "26/SP", "001", true);

        CourseSeedImportResult result = importer.importCourses(List.of(
                new CourseSeedRow("COMP-495", "26/SP", "002")));

        List<Course> courses = courseRepository.findAll();
        assertThat(courses).hasSize(2);

        Course inactiveOldCourse = courseRepository
                .findByCourseCodeAndTermAndSection("COMP-495", "26/SP", "001")
                .orElseThrow();

        Course newCourse = courseRepository
                .findByCourseCodeAndTermAndSection("COMP-495", "26/SP", "002")
                .orElseThrow();

        assertThat(inactiveOldCourse.getCourseId()).isEqualTo(oldCourse.getCourseId());
        assertThat(inactiveOldCourse.isActive()).isFalse();

        assertThat(newCourse.getCourseId()).isNotEqualTo(oldCourse.getCourseId());
        assertThat(newCourse.isActive()).isTrue();

        assertThat(result).isEqualTo(new CourseSeedImportResult(1, 0, 0, 1));
    }

    /**
     * Verifies that importing the same row produces no duplicates.
     */
    @Test
    void testImportCoursesIsIdempotentForSameRows() {
        List<CourseSeedRow> rows = List.of(
                new CourseSeedRow("COMP-495", "26/FA", "H1WW"),
                new CourseSeedRow("COMP-496", "26/FA", "H2WW"));

        CourseSeedImportResult firstResult = importer.importCourses(rows);
        CourseSeedImportResult secondResult = importer.importCourses(rows);

        List<Course> courses = courseRepository.findAll();

        assertThat(courses).hasSize(2);
        assertThat(firstResult).isEqualTo(new CourseSeedImportResult(2, 0, 0, 0));
        assertThat(secondResult).isEqualTo(new CourseSeedImportResult(0, 2, 0, 0));
    }
}
