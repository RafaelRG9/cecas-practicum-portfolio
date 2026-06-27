package edu.franklin.cecas.service;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import edu.franklin.cecas.domain.Category;
import edu.franklin.cecas.domain.Course;
import edu.franklin.cecas.domain.ExtraCreditRequestStatus;
import edu.franklin.cecas.domain.User;
import edu.franklin.cecas.domain.UserRole;
import edu.franklin.cecas.dto.ExtraCreditRequestCreateDTO;
import edu.franklin.cecas.dto.ExtraCreditResponseDTO;
import edu.franklin.cecas.repository.CategoryRepository;
import edu.franklin.cecas.repository.CourseRepository;
import edu.franklin.cecas.repository.ExtraCreditRequestRepository;
import edu.franklin.cecas.repository.UserRepository;
import edu.franklin.cecas.support.MySqlServiceTest;

@MySqlServiceTest
public class ExtraCreditRequestServiceTest {
    
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ExtraCreditRequestService extraCreditRequestService;

    @Autowired
    private ExtraCreditRequestRepository extraCreditRequestRepository;

    private User createTestStudent(String name, String email, Integer studentId) {
        User user = new User();

        user.setFullName(name);
        user.setEmail(email);
        user.setPassword("password");
        user.setStudentId(studentId); //Generates a unique ID, without it the testing rewrites it with the new student for testing A and B students
        user.setProgram("Computer Science");
        user.setIsActive(true);
        user.setMustChangePassword(false);
        user.setRole(UserRole.STUDENT);

        return userRepository.save(user);
    }

    private Course createTestCourse() {
        Course course = new Course();
        
        course.setCourseCode("COMP-201");
        course.setSection("B01");
        course.setTerm("Summer 2026");

        return courseRepository.save(course);
    }

    private Category createTestCategory() {
        Category category = new Category();

        category.setCategoryName("Homework");
        category.setDefaultPoints(10);
        category.setDescription("Standard homework assignment category");

        return categoryRepository.save(category);
    }

    @Test
    void TestCreateRequestSuccesfully() {
        User student = createTestStudent("Test Student", "student@test.com", 1001);
        Course course = createTestCourse();
        Category category = createTestCategory();

        ExtraCreditRequestCreateDTO dto = new ExtraCreditRequestCreateDTO();
        dto.setCourseId(course.getCourseId());
        dto.setCategoryId(category.getCategoryId());
        dto.setDescription("I completed the extra assignment");

        ExtraCreditResponseDTO response = extraCreditRequestService.createRequest(student.getEmail(), dto);

        assertNotNull(response);
        assertEquals(ExtraCreditRequestStatus.PENDING, response.getStatus());
        assertEquals("I completed the extra assignment", response.getDescription());
    }

    @Test
    void testCreateRequestThrowsWhenUserIsNotAStudent() {
        User faculty = createTestStudent("Test Faculty", "faculty@test.com", 1001);
        faculty.setRole(UserRole.CHAIR);
        userRepository.save(faculty);

        Course course = createTestCourse();
        Category category = createTestCategory();

        ExtraCreditRequestCreateDTO dto = new ExtraCreditRequestCreateDTO();
        dto.setCourseId(course.getCourseId());
        dto.setCategoryId(category.getCategoryId());
        dto.setDescription("Attempt by non-student");

        RuntimeException ex = assertThrows(RuntimeException.class, () -> 
            extraCreditRequestService.createRequest(faculty.getEmail(), dto)
        );

        assertEquals("Unauthorized: User is not a student", ex.getMessage());
    }

    @Test
    void testGetRequestsForStudentReturnsOnlyTheirRequests() {
        User studentA = createTestStudent("Student A", "studentA@test.com", 2001);
        User studentB = createTestStudent("Student B", "studentB@test.com", 2002);
        Course course = createTestCourse();
        Category category = createTestCategory();

        ExtraCreditRequestCreateDTO dto = new ExtraCreditRequestCreateDTO();
        dto.setCourseId(course.getCourseId());
        dto.setCategoryId(category.getCategoryId());
        dto.setDescription("Student A Request");

        // I want to test that the list created is for one student and one student alone
        extraCreditRequestService.createRequest(studentA.getEmail(), dto);

        System.out.println("==================================================");
        var allRequests = extraCreditRequestRepository.findAll();
        System.out.println("DEBUG: Total requests in DB: " + allRequests.size());
        if (!allRequests.isEmpty()) {
            System.out.println("DEBUG: Raw Request Data: " + allRequests.get(0).toString());
        }
        System.out.println("==================================================");

        // Student A: should populate
        List<ExtraCreditResponseDTO> requestsA = extraCreditRequestService.getRequestsForStudent(studentA.getEmail());
        assertEquals(1, requestsA.size());
        assertEquals("Student A Request", requestsA.get(0).getDescription());

        // Student B: should return empty
        List<ExtraCreditResponseDTO> requestsB = extraCreditRequestService.getRequestsForStudent(studentB.getEmail());
        assertTrue(requestsB.isEmpty());
    }

    /**
     * Tests that applications for students by UserId are successful still while StudentId is null.
     */
    @Test
    public void testGetRequestsForStudentWorksWhenBusinessStudentIdIsNull() {
        User studentA = createTestStudent("Student A", "studentA-null@test.com", null);
    User studentB = createTestStudent("Student B", "studentB-null@test.com", null);
    Course course = createTestCourse();
    Category category = createTestCategory();

    ExtraCreditRequestCreateDTO dto = new ExtraCreditRequestCreateDTO();
    dto.setCourseId(course.getCourseId());
    dto.setCategoryId(category.getCategoryId());
    dto.setDescription("Student A Null-ID Request");

    extraCreditRequestService.createRequest(studentA.getEmail(), dto);

    List<ExtraCreditResponseDTO> requestsA =
            extraCreditRequestService.getRequestsForStudent(studentA.getEmail());
    assertEquals(1, requestsA.size());
    assertEquals("Student A Null-ID Request", requestsA.get(0).getDescription());

    List<ExtraCreditResponseDTO> requestsB =
            extraCreditRequestService.getRequestsForStudent(studentB.getEmail());
    assertTrue(requestsB.isEmpty());
    }
}
