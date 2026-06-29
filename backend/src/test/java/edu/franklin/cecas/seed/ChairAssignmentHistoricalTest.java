package edu.franklin.cecas.seed;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import edu.franklin.cecas.config.SecurityConfig;
import edu.franklin.cecas.domain.Category;
import edu.franklin.cecas.domain.ChairCourseAssignment;
import edu.franklin.cecas.domain.Course;
import edu.franklin.cecas.domain.ExtraCreditRequest;
import edu.franklin.cecas.domain.ExtraCreditRequestStatus;
import edu.franklin.cecas.domain.User;
import edu.franklin.cecas.domain.UserRole;
import edu.franklin.cecas.repository.CategoryRepository;
import edu.franklin.cecas.repository.ChairCourseAssignmentRepository;
import edu.franklin.cecas.repository.CourseRepository;
import edu.franklin.cecas.repository.ExtraCreditRequestRepository;
import edu.franklin.cecas.repository.UserRepository;
import edu.franklin.cecas.service.CecasUserDetailsService;
import edu.franklin.cecas.service.PasswordService;
import edu.franklin.cecas.support.MySqlDataJpaTest;

@MySqlDataJpaTest
@SuppressWarnings("unused")
@Import({ ChairSeedImporter.class, PasswordService.class, SecurityConfig.class })
public class ChairAssignmentHistoricalTest {

    @Autowired
    private ChairSeedImporter importer;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ExtraCreditRequestRepository extraCreditRequestRepository;

    @Autowired
    private ChairCourseAssignmentRepository assignmentRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @MockitoBean
    private CecasUserDetailsService userDetailsService;

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
        user.setFullName("Test Student");
        user.setProgram("Computer Science");
        user.setPassword(passwordEncoder.encode("StudentPass1!"));
        user.setRole(UserRole.STUDENT);
        user.setStudentId(studentId);
        user.setIsActive(true);
        user.setMustChangePassword(false);
        user.setEmailVerified(true);
        return userRepository.save(user);
    }

    private Course saveCourse(String code, String term, String section, boolean active) {
        Course course = new Course(code, term, section);
        course.setActive(active);
        return courseRepository.save(course);
    }

    private Category saveCategory(String name, String description, int defaultPoints, boolean active) {
        Category category = new Category();
        category.setCategoryName(name);
        category.setDescription(description);
        category.setDefaultPoints(defaultPoints);
        category.setActive(active);
        return categoryRepository.save(category);
    }

    private void saveAssignment(User chair, Course course) {
        assignmentRepository.save(new ChairCourseAssignment(chair, course));
    }

    /**
     * Verifies that synchronizing current Chair assignments does not change the
     * historical Chair and course references already stored on extra credit
     * requests.
     */
    @Test
    void testImportChairsPreservesHistoricalRequestRecordsWhenCurrentAssignmentIsRemoved() {
        User chair = saveChair(
                "grace.hopper@email.franklin.edu",
                "Grace Hopper",
                "Computer Science",
                passwordEncoder.encode("ExistingPass1!"),
                true,
                false);

        User student = saveStudent("student1@email.franklin.edu", 1001);

        Course oldCourse = saveCourse("COMP-294", "26/FA", "H1WW", true);
        Course newCourse = saveCourse("COMP-394", "26/FA", "H1WW", true);
        Category category = saveCategory("Seminar Attendance", "Approved seminar attendance", 5, true);

        saveAssignment(chair, oldCourse);

        ExtraCreditRequest request = new ExtraCreditRequest();
        request.setDescription("Historical request");
        request.setStudent(student);
        request.setChair(chair);
        request.setCourse(oldCourse);
        request.setCategory(category);
        request.setStatus(ExtraCreditRequestStatus.PENDING);
        ExtraCreditRequest savedRequest = extraCreditRequestRepository.save(request);

        ChairSeedImportResult result = importer.importChairs(List.of(
                new ChairSeedRow(
                        "grace.hopper@email.franklin.edu",
                        "Grace Hopper",
                        "Computer Science",
                        Set.of("COMP-394"),
                        "IgnoredTempPassword1!")));

        ExtraCreditRequest reloadedRequest = extraCreditRequestRepository.findById(savedRequest.getId()).orElseThrow();
        List<ChairCourseAssignment> currentAssignments = assignmentRepository.findAllByChairId(chair.getId());

        assertThat(reloadedRequest.getChair().getId()).isEqualTo(chair.getId());
        assertThat(reloadedRequest.getCourse().getCourseId()).isEqualTo(oldCourse.getCourseId());
        assertThat(reloadedRequest.getCategory().getCategoryId()).isEqualTo(category.getCategoryId());

        assertThat(currentAssignments)
                .extracting(a -> a.getCourse().getCourseId())
                .containsExactly(newCourse.getCourseId());

        assertThat(result).isEqualTo(new ChairSeedImportResult(0, 0, 1, 0, 0, 1, 1));
    }
}
