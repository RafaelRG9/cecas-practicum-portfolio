package edu.franklin.cecas.seed;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import edu.franklin.cecas.domain.Category;
import edu.franklin.cecas.domain.ChairCourseAssignment;
import edu.franklin.cecas.domain.Course;
import edu.franklin.cecas.domain.User;
import edu.franklin.cecas.domain.UserRole;
import edu.franklin.cecas.exception.SeedSynchronizationException;
import edu.franklin.cecas.exception.SeedValidationException;
import edu.franklin.cecas.repository.CategoryRepository;
import edu.franklin.cecas.repository.ChairCourseAssignmentRepository;
import edu.franklin.cecas.repository.CourseRepository;
import edu.franklin.cecas.repository.UserRepository;
import edu.franklin.cecas.service.CecasUserDetailsService;
import edu.franklin.cecas.support.MySqlServiceTest;

@MySqlServiceTest
public class SeedServiceTest {

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
    private CecasUserDetailsService userDetailsService;

    private static final Path seedDir = createSeedDirectory();

    @DynamicPropertySource
    static void registerProperties(DynamicPropertyRegistry registry) {
        registry.add("app.seed.path", () -> seedDir.toString());
        registry.add("app.seed.enabled", () -> "false");
    }

    @BeforeEach
    void clearSeedDirectory() throws IOException {
        try (var stream = Files.list(seedDir)) {
            for (Path path : stream.toList()) {
                Files.deleteIfExists(path);
            }
        }
    }

    @AfterEach
    void cleanDatabaseState() {
        assignmentRepository.deleteAll();
        userRepository.deleteAll();
        categoryRepository.deleteAll();
        courseRepository.deleteAll();
    }

    private static Path createSeedDirectory() {
        try {
            return Files.createTempDirectory("seed-service-test");
        } catch (IOException ex) {
            throw new IllegalStateException("Failed to create temporary seed directory.", ex);
        }
    }

    private Path writeCsv(String fileName, String contents) throws IOException {
        Path file = seedDir.resolve(fileName);
        Files.writeString(file, contents);
        return file;
    }

    private void writeValidSeedFiles() throws IOException {
        writeCsv("courses.csv", """
                course_code,term,section
                COMP-294,26/FA,H1WW
                COMP-294,26/FA,H2WW
                COMP-495,26/FA,H1WW
                """);

        writeCsv("chairs.csv", """
                email,full_name,program,course_codes,temp_password
                grace.hopper@email.franklin.edu,Grace Hopper,Computer Science,COMP-294,ChairTemp01!
                alan.turing@email.franklin.edu,Alan Turing,Computer Science,COMP-495,ChairTemp02!
                """);

        writeCsv("categories.csv", """
                category_name,description,default_points
                Seminar Attendance,Approved seminar attendance,5
                Research Presentation,Presented research poster,10
                """);
    }

    private User saveStudent(String email, int studentId) {
        User user = new User();
        user.setEmail(email);
        user.setFullName("Existing Student");
        user.setProgram("Computer Science");
        user.setPassword(passwordEncoder.encode("StudentPass1!"));
        user.setRole(UserRole.STUDENT);
        user.setStudentId(studentId);
        user.setIsActive(true);
        user.setMustChangePassword(false);
        user.setEmailVerified(true);
        return userRepository.save(user);
    }

    private User findUserByEmailOrThrow(String email) {
        return userRepository.findByEmailIgnoreCase(email).orElseThrow();
    }

    private Category findCategoryByNameOrThrow(String name) {
        return categoryRepository.findByCategoryNameIgnoreCase(name).orElseThrow();
    }

    /**
     * Verifies that the service loads course, category, and chair seed data
     * together in one run.
     */
    @Test
    void testSeedLoadsAllThreeFilesIntoDatabase() throws IOException {
        writeValidSeedFiles();

        SeedRunResult result = seedService.seed();

        List<Course> courses = courseRepository.findAll();
        List<Category> categories = categoryRepository.findAll();
        List<User> chairs = userRepository.findAllByRoleAndIsActiveTrue(UserRole.CHAIR);

        User grace = findUserByEmailOrThrow("grace.hopper@email.franklin.edu");
        User alan = findUserByEmailOrThrow("alan.turing@email.franklin.edu");

        List<ChairCourseAssignment> graceAssignments = assignmentRepository.findAllByChairId(grace.getId());
        List<ChairCourseAssignment> alanAssignments = assignmentRepository.findAllByChairId(alan.getId());

        assertThat(courses).hasSize(3);
        assertThat(categories).hasSize(2);
        assertThat(chairs).hasSize(2);

        assertThat(findCategoryByNameOrThrow("Seminar Attendance").getDefaultPoints()).isEqualTo(5);
        assertThat(findCategoryByNameOrThrow("Research Presentation").getDefaultPoints()).isEqualTo(10);

        assertThat(grace.getMustChangePassword()).isTrue();
        assertThat(passwordEncoder.matches("ChairTemp01!", grace.getPassword())).isTrue();

        assertThat(graceAssignments).hasSize(2);
        assertThat(alanAssignments).hasSize(1);

        assertThat(result.courses()).isEqualTo(new CourseSeedImportResult(3, 0, 0, 0));
        assertThat(result.categories()).isEqualTo(new CategorySeedImportResult(2, 0, 0, 0, 0));
        assertThat(result.chairs()).isEqualTo(new ChairSeedImportResult(2, 0, 0, 0, 0, 3, 0));
    }

    /**
     * Verifies that re-running the same seed files does not create duplicate
     * records.
     */
    @Test
    void testSeedIsIdempotentForSameFiles() throws IOException {
        writeValidSeedFiles();

        SeedRunResult firstResult = seedService.seed();
        SeedRunResult secondResult = seedService.seed();

        User grace = findUserByEmailOrThrow("grace.hopper@email.franklin.edu");
        User alan = findUserByEmailOrThrow("alan.turing@email.franklin.edu");

        assertThat(courseRepository.findAll()).hasSize(3);
        assertThat(categoryRepository.findAll()).hasSize(2);
        assertThat(userRepository.findAllByRoleAndIsActiveTrue(UserRole.CHAIR)).hasSize(2);
        assertThat(assignmentRepository.findAllByChairId(grace.getId())).hasSize(2);
        assertThat(assignmentRepository.findAllByChairId(alan.getId())).hasSize(1);

        assertThat(firstResult.courses()).isEqualTo(new CourseSeedImportResult(3, 0, 0, 0));
        assertThat(firstResult.categories()).isEqualTo(new CategorySeedImportResult(2, 0, 0, 0, 0));
        assertThat(firstResult.chairs()).isEqualTo(new ChairSeedImportResult(2, 0, 0, 0, 0, 3, 0));

        assertThat(secondResult.courses()).isEqualTo(new CourseSeedImportResult(0, 3, 0, 0));
        assertThat(secondResult.categories()).isEqualTo(new CategorySeedImportResult(0, 0, 2, 0, 0));
        assertThat(secondResult.chairs()).isEqualTo(new ChairSeedImportResult(0, 0, 2, 0, 0, 0, 0));
    }

    /**
     * Verifies that invalid seed file content fails before synchronization and does
     * not write any database rows.
     */
    @Test
    void testSeedRejectsInvalidFilesWithoutWritingDatabaseRows() throws IOException {
        writeCsv("courses.csv", """
                course_code,term,section
                COMP-294,26/FA,H1WW
                """);

        writeCsv("chairs.csv", """
                email,full_name,program,course_codes,temp_password
                grace.hopper@email.franklin.edu,Grace Hopper,Computer Science,COMP-999,ChairTemp01!
                """);

        writeCsv("categories.csv", """
                category_name,description,default_points
                Seminar Attendance,Approved seminar attendance,-1
                """);

        assertThatThrownBy(() -> seedService.seed())
                .isInstanceOf(SeedValidationException.class);

        assertThat(courseRepository.findAll()).isEmpty();
        assertThat(categoryRepository.findAll()).isEmpty();
        assertThat(userRepository.findAll()).isEmpty();
        assertThat(assignmentRepository.findAll()).isEmpty();
    }

    /**
     * Verifies that synchronization failures during Chair import roll back earlier
     * course and category writes from the same seed run.
     */
    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void testSeedRollsBackEarlierWritesWhenChairSynchronizationFails() throws IOException {
        saveStudent("grace.hopper@email.franklin.edu", 1001);

        writeCsv("courses.csv", """
                course_code,term,section
                COMP-294,26/FA,H1WW
                """);

        writeCsv("chairs.csv", """
                email,full_name,program,course_codes,temp_password
                grace.hopper@email.franklin.edu,Grace Hopper,Computer Science,COMP-294,ChairTemp01!
                """);

        writeCsv("categories.csv", """
                category_name,description,default_points
                Seminar Attendance,Approved seminar attendance,5
                """);

        assertThatThrownBy(() -> seedService.seed())
                .isInstanceOf(SeedSynchronizationException.class)
                .hasMessageContaining("already belongs to a non-Chair user");

        assertThat(courseRepository.findAll()).isEmpty();
        assertThat(categoryRepository.findAll()).isEmpty();
        assertThat(userRepository.findAll()).hasSize(1);
        assertThat(assignmentRepository.findAll()).isEmpty();
    }

    /**
     * Verifies that a reseed updates synchronized data while preserving existing
     * Chair credentials.
     */
    @Test
    void testSeedResynchronizesDataWithoutOverwritingExistingChairCredentials() throws IOException {
        writeValidSeedFiles();
        seedService.seed();

        User originalGrace = findUserByEmailOrThrow("grace.hopper@email.franklin.edu");
        String originalPasswordHash = originalGrace.getPassword();

        writeCsv("courses.csv", """
                course_code,term,section
                COMP-294,26/FA,H1WW
                COMP-495,26/FA,H1WW
                """);

        writeCsv("chairs.csv", """
                email,full_name,program,course_codes,temp_password
                grace.hopper@email.franklin.edu,Grace Hopper Updated,Data Science,COMP-495,NewTempPassword99!
                alan.turing@email.franklin.edu,Alan Turing,Computer Science,COMP-495,ChairTemp02!
                """);

        writeCsv("categories.csv", """
                category_name,description,default_points
                Seminar Attendance,Updated description,6
                Research Presentation,Presented research poster,10
                """);

        SeedRunResult result = seedService.seed();

        User updatedGrace = findUserByEmailOrThrow("grace.hopper@email.franklin.edu");
        Category seminar = findCategoryByNameOrThrow("Seminar Attendance");
        List<ChairCourseAssignment> assignments = assignmentRepository.findAllByChairId(updatedGrace.getId());

        assertThat(updatedGrace.getFullName()).isEqualTo("Grace Hopper Updated");
        assertThat(updatedGrace.getProgram()).isEqualTo("Data Science");
        assertThat(updatedGrace.getPassword()).isEqualTo(originalPasswordHash);
        assertThat(passwordEncoder.matches("NewTempPassword99!", updatedGrace.getPassword())).isFalse();

        assertThat(seminar.getDescription()).isEqualTo("Updated description");
        assertThat(seminar.getDefaultPoints()).isEqualTo(6);

        assertThat(assignments)
                .extracting(a -> a.getCourse().getCourseCode())
                .containsExactly("COMP-495");

        assertThat(result.categories().updated()).isEqualTo(1);
    }
}
