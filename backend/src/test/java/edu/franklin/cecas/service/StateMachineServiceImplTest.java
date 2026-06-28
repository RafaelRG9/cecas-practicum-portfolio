package edu.franklin.cecas.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import edu.franklin.cecas.domain.Category;
import edu.franklin.cecas.domain.Course;
import edu.franklin.cecas.domain.ExtraCreditRequest;
import edu.franklin.cecas.domain.ExtraCreditRequestStatus;
import edu.franklin.cecas.domain.User;
import edu.franklin.cecas.domain.UserRole;
import edu.franklin.cecas.repository.CategoryRepository;
import edu.franklin.cecas.repository.CourseRepository;
import edu.franklin.cecas.repository.ExtraCreditRequestRepository;
import edu.franklin.cecas.repository.UserRepository;
import edu.franklin.cecas.support.MySqlServiceTest;


@MySqlServiceTest
class StateMachineServiceImplTest {
    @Autowired
    private StateMachineService stateMachineService;

    @Autowired
    private ExtraCreditRequestRepository requestRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    private User chairUser;
    private User studentUser;
    private ExtraCreditRequest savedRequest;

    @BeforeEach
    void setUp() {
        chairUser = new User();
        chairUser.setFullName("Chair User");
        chairUser.setEmail("chair@test.com");
        chairUser.setPassword("password1!");
        chairUser.setRole(UserRole.CHAIR);
        chairUser.setIsActive(true);
        chairUser.setEmailVerified(true);
        chairUser.setMustChangePassword(false);
        chairUser.setProgram("N/A");
        chairUser = userRepository.save(chairUser);

        studentUser = new User();
        studentUser.setFullName("Student User");
        studentUser.setEmail("student@test.com");
        studentUser.setPassword("password2!");
        studentUser.setRole(UserRole.STUDENT);
        studentUser.setStudentId(1001);
        studentUser.setProgram("Computer Science");
        studentUser.setIsActive(true);
        studentUser.setEmailVerified(true);
        studentUser.setMustChangePassword(false);
        studentUser = userRepository.save(studentUser);

        Course course = new Course();
        course.setCourseCode("COMP-311");
        course.setTerm("26/SU");
        course.setSection("Q1WW");
        course = courseRepository.save(course);

        Category category = new Category();
        category.setCategoryName("Seminar");
        category.setDescription("Seminar attendance");
        category.setDefaultPoints(10);
        category = categoryRepository.save(category);

        ExtraCreditRequest request = new ExtraCreditRequest();
        request.setDescription("Attended an approved seminar and requesting pre-approval.");
        request.setStudent(studentUser);
        request.setCourse(course);
        request.setCategory(category);
        request.setStatus(ExtraCreditRequestStatus.PENDING);
        savedRequest = requestRepository.save(request);
    }

    
    @Test
    void preApproveRequest_ShouldUpdateStatusToPreApproved_WhenActorIsChair() {
        ExtraCreditRequest result = stateMachineService.preApproveRequest(savedRequest.getId(), chairUser);

        assertThat(result.getStatus()).isEqualTo(ExtraCreditRequestStatus.PRE_APPROVED);
        
        ExtraCreditRequest databaseCheck = requestRepository.findById(savedRequest.getId()).orElseThrow();
        
        assertThat(databaseCheck.getStatus()).isEqualTo(ExtraCreditRequestStatus.PRE_APPROVED);
    }

    @Test
    void preApproveRequest_ShouldThrowException_WhenActorIsStudent() {
        assertThatThrownBy(() -> stateMachineService.preApproveRequest(savedRequest.getId(), studentUser))
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("Unauthorized: Only a Chair can pre-approve requests.");
    }
}
