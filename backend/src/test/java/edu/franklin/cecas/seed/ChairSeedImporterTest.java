package edu.franklin.cecas.seed;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import edu.franklin.cecas.config.SecurityConfig;
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
import edu.franklin.cecas.service.PasswordService;
import edu.franklin.cecas.support.MySqlDataJpaTest;

@MySqlDataJpaTest
@Import({ ChairSeedImporter.class, PasswordService.class, SecurityConfig.class })
public class ChairSeedImporterTest {

    @Autowired
    private ChairSeedImporter importer;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private ChairCourseAssignmentRepository assignmentRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @MockitoBean
    private CecasUserDetailsService userDetailsService;

    private Course saveCourse(String code, String term, String section, boolean active) {
        Course course = new Course(code, term, section);
        course.setActive(active);
        return courseRepository.save(course);
    }

    private User saveChair(String email, String fullName, String program,
            String encodedPassword, boolean active, boolean mustChangePassword) {
        User user = new User();
        user.setEmail(email);
        user.setFullName(fullName);
        user.setProgram(program);
        user.setPassword(encodedPassword);
        user.setRole(UserRole.CHAIR);
        user.setStudentId(null);
        user.setIsActive(active);
        user.setMustChangePassword(mustChangePassword);
        user.setEmailVerified(true);
        return userRepository.save(user);
    }

    private User saveStudent(String email, int studentId) {
        User user = new User();
        user.setEmail(email);
        user.setFullName("Derek Finnell");
        user.setProgram("Computer Science");
        user.setPassword(passwordEncoder.encode("DerekPass1!"));
        user.setRole(UserRole.STUDENT);
        user.setStudentId(studentId);
        user.setIsActive(true);
        user.setMustChangePassword(false);
        user.setEmailVerified(true);
        return userRepository.save(user);
    }

    private void saveAssignment(User chair, Course course) {
        assignmentRepository.save(new ChairCourseAssignment(chair, course));
    }

    /**
     * Verifies that the importer can create a chair, hash the password, and expand
     * a course code to many active course rows.
     */
    @Test
    void testImportChairsInsertsNewChairAndAssignsAllActiveMatchingCourses() {
        Course fall1 = saveCourse("COMP-294", "26/FA", "H1WW", true);
        Course fall2 = saveCourse("COMP-294", "26/FA", "H2WW", true);
        Course spring1 = saveCourse("COMP-294", "27/SP", "H1WW", true);
        saveCourse("COMP-294", "25/SP", "H1WW", false);

        ChairSeedImportResult result = importer.importChairs(List.of(
                new ChairSeedRow(
                        "grace.hopper@email.franklin.edu",
                        "Grace Hopper",
                        "Computer Science",
                        Set.of("COMP-294"),
                        "ChairTemp01!")));

        User saved = userRepository.findByEmailIgnoreCase("grace.hopper@email.franklin.edu").orElseThrow();
        List<ChairCourseAssignment> assignments = assignmentRepository.findAllByChairId(saved.getId());

        assertThat(saved.getRole()).isEqualTo(UserRole.CHAIR);
        assertThat(saved.getStudentId()).isNull();
        assertThat(saved.getFullName()).isEqualTo("Grace Hopper");
        assertThat(saved.getProgram()).isEqualTo("Computer Science");
        assertThat(saved.getIsActive()).isTrue();
        assertThat(saved.getMustChangePassword()).isTrue();
        assertThat(saved.getPassword()).isNotEqualTo("ChairTemp01!");
        assertThat(passwordEncoder.matches("ChairTemp01!", saved.getPassword())).isTrue();

        assertThat(assignments)
                .extracting(a -> a.getCourse().getCourseId())
                .containsExactlyInAnyOrder(
                        fall1.getCourseId(),
                        fall2.getCourseId(),
                        spring1.getCourseId());

        assertThat(result).isEqualTo(new ChairSeedImportResult(1, 0, 0, 0, 0, 3, 0));
    }

    /**
     * Verify that an existing chair can be updated without overwriting credentials.
     */
    @Test
    void testImportChairsUpdatesExistingChairProfileWithoutOverwritingPasswordOrMustChangeFlag() {
        User existing = saveChair(
                "derek.finnell@email.franklin.edu",
                "Derek Finnell",
                "Computer Science",
                passwordEncoder.encode("ExistingPass1!"),
                true,
                false);

        Course course = saveCourse("COMP-294", "26/FA", "H1WW", true);

        ChairSeedImportResult result = importer.importChairs(List.of(
                new ChairSeedRow(
                        "derek.finnell@email.franklin.edu",
                        "Derek Changename",
                        "Data Science",
                        Set.of("COMP-294"),
                        "ThisShouldntWork!")));

        User updated = userRepository.findById(existing.getId()).orElseThrow();

        assertThat(updated.getFullName()).isEqualTo("Derek Changename");
        assertThat(updated.getProgram()).isEqualTo("Data Science");
        assertThat(updated.getIsActive()).isTrue();
        assertThat(updated.getMustChangePassword()).isFalse();
        assertThat(passwordEncoder.matches("ExistingPass1!", updated.getPassword())).isTrue();
        assertThat(passwordEncoder.matches("ThisShouldntWork!", updated.getPassword())).isFalse();

        assertThat(result).isEqualTo(new ChairSeedImportResult(0, 1, 0, 0, 0, 1, 0));
    }

    /**
     * Verifies that assignment-only synchronization adds and removes the correct
     * course assignments while still counting the Chair record as unchanged.
     */
    @Test
    void testImportChairsCountsAssignmentOnlyChangesAsUnchanged() {
        User chair = saveChair(
                "alan.turing@email.franklin.edu",
                "Alan Turing",
                "Computer Science",
                passwordEncoder.encode("ExistingPass1!"),
                true,
                false);

        Course oldCourse = saveCourse("COMP-294", "26/FA", "H1WW", true);
        Course newCourse = saveCourse("COMP-394", "26/FA", "H1WW", true);
        saveAssignment(chair, oldCourse);

        ChairSeedImportResult result = importer.importChairs(List.of(
                new ChairSeedRow(
                        "alan.turing@email.franklin.edu",
                        "Alan Turing",
                        "Computer Science",
                        Set.of("COMP-394"),
                        "IgnoredTempPassword1!")));

        List<ChairCourseAssignment> assignments = assignmentRepository.findAllByChairId(chair.getId());

        assertThat(assignments)
                .extracting(a -> a.getCourse().getCourseId())
                .containsExactly(newCourse.getCourseId());

        assertThat(result).isEqualTo(new ChairSeedImportResult(0, 0, 1, 0, 0, 1, 1));
    }

    /**
     * Verifies that an inactive matching Chair account is reactivated, updated, and
     * reassigned without creating a new account or overwriting credentials.
     */
    @Test
    void importChairsReactivatesExistingInactiveChairAndPreservesCredentials() {
        User chair = saveChair(
                "derek1@email.franklin.edu",
                "Old Derek",
                "Computer Science",
                passwordEncoder.encode("ExistingPass1!"),
                false,
                false);

        Course course = saveCourse("COMP-394", "26/FA", "H1WW", true);

        ChairSeedImportResult result = importer.importChairs(List.of(
                new ChairSeedRow(
                        "derek1@email.franklin.edu",
                        "New Derek",
                        "Data Science",
                        Set.of("COMP-394"),
                        "IgnoredTempPassword1!")));

        User updated = userRepository.findById(chair.getId()).orElseThrow();

        assertThat(updated.getIsActive()).isTrue();
        assertThat(updated.getFullName()).isEqualTo("New Derek");
        assertThat(updated.getProgram()).isEqualTo("Data Science");
        assertThat(passwordEncoder.matches("ExistingPass1!", updated.getPassword())).isTrue();
        assertThat(updated.getMustChangePassword()).isFalse();

        assertThat(result).isEqualTo(new ChairSeedImportResult(0, 0, 0, 1, 0, 1, 0));
    }

    /**
     * Verifies that the importer rejects a Chair row when the email already belongs
     * to an existing non-CHAIR user, without creating assignments.
     */
    @Test
    void testImportChairsRejectsEmailConflictWithExistingNonChairUser() {
        saveStudent("chair1@email.franklin.edu", 1001);
        saveCourse("COMP-495", "26/FA", "H1WW", true);

        assertThatThrownBy(() -> importer.importChairs(List.of(
                new ChairSeedRow(
                        "chair1@email.franklin.edu",
                        "Donald Knuth",
                        "Computer Science",
                        Set.of("COMP-495"),
                        "ChairTemp01!"))))
                .isInstanceOf(SeedSynchronizationException.class)
                .hasMessageContaining("already belongs to a non-Chair user");

        assertThat(userRepository.findAll()).hasSize(1);
        assertThat(assignmentRepository.findAll()).isEmpty();
    }

    /**
     * Verifies that an active Chair missing from the current seed is deactivated
     * and loses current course assignments.
     */
    @Test
    void testImportChairsDeactivatesMissingChairAndRemovesCurrentAssignments() {
        User chair = saveChair(
                "grace.hopper@email.franklin.edu",
                "Grace Hopper",
                "Computer Science",
                passwordEncoder.encode("ExistingPass1!"),
                true,
                false);

        Course course = saveCourse("COMP-294", "26/FA", "Q1WW", true);
        saveAssignment(chair, course);

        ChairSeedImportResult result = importer.importChairs(List.of());

        User updated = userRepository.findById(chair.getId()).orElseThrow();

        assertThat(updated.getIsActive()).isFalse();
        assertThat(assignmentRepository.findAllByChairId(chair.getId())).isEmpty();
        assertThat(result).isEqualTo(new ChairSeedImportResult(0, 0, 0, 0, 1, 0, 0));
    }

    /**
     * Verifies that a changed chair email deactivates the old user and inserts a
     * new.
     */
    @Test
    void testImportChairsTreatsChangedEmailAsNewChairAndDeactivatesOldChair() {
        User oldChair = saveChair(
                "old@email.franklin.edu",
                "Barbara Liskov",
                "Computer Science",
                passwordEncoder.encode("ExistingPass1!"),
                true,
                false);

        Course course = saveCourse("COMP-394", "27/FA", "Q1WW", true);

        ChairSeedImportResult result = importer.importChairs(List.of(
                new ChairSeedRow(
                        "new@email.franklin.edu",
                        "Barbara Liskov",
                        "Computer Science",
                        Set.of("COMP-394"),
                        "ChairTemp01!")));

        User inactiveOld = userRepository.findById(oldChair.getId()).orElseThrow();
        User newChair = userRepository.findByEmailIgnoreCase("new@email.franklin.edu").orElseThrow();

        assertThat(inactiveOld.getIsActive()).isFalse();
        assertThat(newChair.getId()).isNotEqualTo(oldChair.getId());
        assertThat(newChair.getIsActive()).isTrue();

        assertThat(result).isEqualTo(new ChairSeedImportResult(1, 0, 0, 0, 1, 1, 0));
    }

    /**
     * Verifies that the system is indempotent and duplicate records are not
     * created.
     */
    @Test
    void testImportChairsIsIdempotentForSameRows() {
        saveCourse("COMP-495", "26/SU", "F1WW", true);
        saveCourse("COMP-495", "26/SU", "F2WW", true);

        List<ChairSeedRow> rows = List.of(
                new ChairSeedRow(
                        "edsger.dijkstra@email.franklin.edu",
                        "Edsger Dijkstra",
                        "Computer Science",
                        Set.of("COMP-495"),
                        "ChairTemp01!"));

        ChairSeedImportResult first = importer.importChairs(rows);
        ChairSeedImportResult second = importer.importChairs(rows);

        User chair = userRepository.findByEmailIgnoreCase("edsger.dijkstra@email.franklin.edu").orElseThrow();
        List<ChairCourseAssignment> assignments = assignmentRepository.findAllByChairId(chair.getId());

        assertThat(assignments).hasSize(2);
        assertThat(first).isEqualTo(new ChairSeedImportResult(1, 0, 0, 0, 0, 2, 0));
        assertThat(second).isEqualTo(new ChairSeedImportResult(0, 0, 1, 0, 0, 0, 0));
    }
}
