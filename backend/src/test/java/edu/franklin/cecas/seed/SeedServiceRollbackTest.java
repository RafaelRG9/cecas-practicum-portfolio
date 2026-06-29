package edu.franklin.cecas.seed;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import edu.franklin.cecas.domain.Category;
import edu.franklin.cecas.domain.ChairCourseAssignment;
import edu.franklin.cecas.domain.Course;
import edu.franklin.cecas.domain.User;
import edu.franklin.cecas.domain.UserRole;
import edu.franklin.cecas.exception.SeedSynchronizationException;
import edu.franklin.cecas.repository.CategoryRepository;
import edu.franklin.cecas.repository.ChairCourseAssignmentRepository;
import edu.franklin.cecas.repository.CourseRepository;
import edu.franklin.cecas.repository.UserRepository;
import edu.franklin.cecas.service.CecasUserDetailsService;
import edu.franklin.cecas.support.MySqlServiceTest;

@MySqlServiceTest
public class SeedServiceRollbackTest {

    @Autowired
    private SeedService seedService;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ChairCourseAssignmentRepository assignmentRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @MockitoBean
    private SeedDataParser seedDataParser;

    @MockitoBean
    private CourseSeedImporter courseSeedImporter;

    @MockitoBean
    private CategorySeedImporter categorySeedImporter;

    @MockitoBean
    private ChairSeedImporter chairSeedImporter;

    @MockitoBean
    private CecasUserDetailsService userDetailsService;

    @AfterEach
    void cleanDatabaseState() {
        assignmentRepository.deleteAll();
        userRepository.deleteAll();
        categoryRepository.deleteAll();
        courseRepository.deleteAll();
    }

    private ParsedSeedData validParsedSeedData() {
        return new ParsedSeedData(
                List.of(new CourseSeedRow("COMP-294", "26/FA", "H1WW")),
                List.of(new ChairSeedRow(
                        "grace.hopper@email.franklin.edu",
                        "Grace Hopper",
                        "Computer Science",
                        Set.of("COMP-294"),
                        "ChairTemp01!")),
                List.of(new CategorySeedRow(
                        "Seminar Attendance",
                        "Approved seminar attendance",
                        5)));
    }

    private Course saveCourseEntity(String code, String term, String section, boolean active) {
        Course course = new Course(code, term, section);
        course.setActive(active);
        return courseRepository.save(course);
    }

    private Category saveCategoryEntity(String name, String description, int defaultPoints, boolean active) {
        Category category = new Category();
        category.setCategoryName(name);
        category.setDescription(description);
        category.setDefaultPoints(defaultPoints);
        category.setActive(active);
        return categoryRepository.save(category);
    }

    private User saveChairEntity(String email, String fullName, String program, String rawPassword) {
        User user = new User();
        user.setEmail(email);
        user.setFullName(fullName);
        user.setProgram(program);
        user.setPassword(passwordEncoder.encode(rawPassword));
        user.setRole(UserRole.CHAIR);
        user.setStudentId(null);
        user.setIsActive(true);
        user.setMustChangePassword(true);
        user.setEmailVerified(true);
        return userRepository.save(user);
    }

    /**
     * Verifies that a failure during course synchronization rolls back all writes
     * from the seed transaction.
     */
    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void testSeedRollsBackAllWritesWhenCourseSynchronizationFails() {
        when(seedDataParser.parseAll()).thenReturn(validParsedSeedData());

        when(courseSeedImporter.importCourses(anyList())).thenAnswer(invocation -> {
            saveCourseEntity("COMP-294", "26/FA", "H1WW", true);
            throw new SeedSynchronizationException("Simulated course synchronization failure.");
        });

        assertThatThrownBy(() -> seedService.seed())
                .isInstanceOf(SeedSynchronizationException.class)
                .hasMessageContaining("course synchronization failure");

        assertThat(courseRepository.findAll()).isEmpty();
        assertThat(categoryRepository.findAll()).isEmpty();
        assertThat(userRepository.findAll()).isEmpty();
        assertThat(assignmentRepository.findAll()).isEmpty();
    }

    /**
     * Verifies that a failure during category synchronization rolls back earlier
     * course writes and any partial category writes from the same seed transaction.
     */
    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void testSeedRollsBackAllWritesWhenCategorySynchronizationFails() {
        when(seedDataParser.parseAll()).thenReturn(validParsedSeedData());

        when(courseSeedImporter.importCourses(anyList())).thenAnswer(invocation -> {
            saveCourseEntity("COMP-294", "26/FA", "H1WW", true);
            return new CourseSeedImportResult(1, 0, 0, 0);
        });

        when(categorySeedImporter.importCategories(anyList())).thenAnswer(invocation -> {
            saveCategoryEntity("Seminar Attendance", "Approved seminar attendance", 5, true);
            throw new SeedSynchronizationException("Simulated category synchronization failure.");
        });

        assertThatThrownBy(() -> seedService.seed())
                .isInstanceOf(SeedSynchronizationException.class)
                .hasMessageContaining("category synchronization failure");

        assertThat(courseRepository.findAll()).isEmpty();
        assertThat(categoryRepository.findAll()).isEmpty();
        assertThat(userRepository.findAll()).isEmpty();
        assertThat(assignmentRepository.findAll()).isEmpty();
    }

    /**
     * Verifies that a failure during Chair assignment synchronization rolls back
     * all earlier course, category, Chair, and assignment writes.
     */
    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void testSeedRollsBackAllWritesWhenAssignmentSynchronizationFails() {
        when(seedDataParser.parseAll()).thenReturn(validParsedSeedData());

        when(courseSeedImporter.importCourses(anyList())).thenAnswer(invocation -> {
            saveCourseEntity("COMP-294", "26/FA", "H1WW", true);
            return new CourseSeedImportResult(1, 0, 0, 0);
        });

        when(categorySeedImporter.importCategories(anyList())).thenAnswer(invocation -> {
            saveCategoryEntity("Seminar Attendance", "Approved seminar attendance", 5, true);
            return new CategorySeedImportResult(1, 0, 0, 0, 0);
        });

        when(chairSeedImporter.importChairs(anyList())).thenAnswer(invocation -> {
            User chair = saveChairEntity(
                    "grace.hopper@email.franklin.edu",
                    "Grace Hopper",
                    "Computer Science",
                    "ChairTemp01!");

            Course course = courseRepository.findAll().get(0);
            assignmentRepository.save(new ChairCourseAssignment(chair, course));

            throw new SeedSynchronizationException("Simulated assignment synchronization failure.");
        });

        assertThatThrownBy(() -> seedService.seed())
                .isInstanceOf(SeedSynchronizationException.class)
                .hasMessageContaining("assignment synchronization failure");

        assertThat(courseRepository.findAll()).isEmpty();
        assertThat(categoryRepository.findAll()).isEmpty();
        assertThat(userRepository.findAll()).isEmpty();
        assertThat(assignmentRepository.findAll()).isEmpty();
    }
}
