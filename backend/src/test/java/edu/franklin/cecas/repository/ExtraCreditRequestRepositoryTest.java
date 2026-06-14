package edu.franklin.cecas.repository;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import edu.franklin.cecas.domain.Category;
import edu.franklin.cecas.domain.Course;
import edu.franklin.cecas.domain.ExtraCreditRequest;
import edu.franklin.cecas.domain.ExtraCreditRequestStatus;
import edu.franklin.cecas.domain.User;
import edu.franklin.cecas.domain.UserRole;
import edu.franklin.cecas.support.MySqlDataJpaTest;

@MySqlDataJpaTest
public class ExtraCreditRequestRepositoryTest {

    @Autowired
    private ExtraCreditRequestRepository extraCreditRequestRepository;
    
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private CategoryRepository categoryRepository;
    
    private User createTestStudent() {
        User user = new User();
        user.setFullName("Test Student");
        user.setEmail("student@test.com");
        user.setPassword("123456");
        user.setRole(UserRole.STUDENT);
        user.setStudentId(12345);
        user.setProgram("Computer Science");
        user.setIsActive(true);
        user.setEmailVerified(true);
        user.setMustChangePassword(false);
        return user;
    }

    private Course createTestCourse() {
        Course course = new Course();
        course.setCourseCode("COMP 311"); //cooked class
        course.setTerm("Summer 2026");
        course.setSection("01");
        return course;
    }

    private Category createTestCategory() {
        Category category = new Category();
        category.setCategoryName("Volunteer");
        category.setDescription("Volunteer work");
        category.setDefaultPoints(10);
        return category;
    }

    @Test
    public void testFindByStudent_StudentId() {

        User student = createTestStudent();
        userRepository.save(student);

        Course course = createTestCourse();
        courseRepository.save(course);

        Category category = createTestCategory();
        categoryRepository.save(category);

        ExtraCreditRequest request = new ExtraCreditRequest();
        request.setDescription("Test request");
        request.setStudent(student);
        request.setCourse(course);
        request.setCategory(category);
        request.setStatus(ExtraCreditRequestStatus.PENDING);

        extraCreditRequestRepository.save(request);

        List<ExtraCreditRequest> result = extraCreditRequestRepository.findByStudent_StudentId(student.getStudentId());

        assertThat(result).isNotNull();
    }

    //@Test
    //public void test findByChair_Id()

    @Test
    public void testFindByCourse_CourseId() {

        Course course = createTestCourse();
        courseRepository.save(course);

        User student = createTestStudent();
        userRepository.save(student);

        Category category = createTestCategory();
        categoryRepository.save(category);

        ExtraCreditRequest request = new ExtraCreditRequest();
        request.setDescription("Test request");
        request.setStudent(student);
        request.setCourse(course);
        request.setCategory(category);
        request.setStatus(ExtraCreditRequestStatus.PENDING);

        extraCreditRequestRepository.save(request);

        List<ExtraCreditRequest> result = extraCreditRequestRepository.findByCourse_CourseId(course.getCourseId());

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getCourse().getCourseId()).isEqualTo(course.getCourseId());
    }

    @Test
    public void testFindByCategory_CategoryId() {

        Category category = createTestCategory();
        categoryRepository.save(category);

        User student = createTestStudent();
        userRepository.save(student);

        Course course = createTestCourse();
        courseRepository.save(course);

        ExtraCreditRequest request = new ExtraCreditRequest();
        request.setDescription("Test request");
        request.setStudent(student);
        request.setCourse(course);
        request.setCategory(category);
        request.setStatus(ExtraCreditRequestStatus.PENDING);

        extraCreditRequestRepository.save(request);

        List<ExtraCreditRequest> result = extraCreditRequestRepository.findByCategory_CategoryId(category.getCategoryId());

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getCategory().getCategoryId()).isEqualTo(category.getCategoryId());
    }

    @Test
    public void testFindByStatus() {

        User student = createTestStudent();
        userRepository.save(student);

        Course course = createTestCourse();
        courseRepository.save(course);

        Category category = createTestCategory();
        categoryRepository.save(category);

        ExtraCreditRequest request = new ExtraCreditRequest();
        request.setDescription("Test request");
        request.setStudent(student);
        request.setCourse(course);
        request.setCategory(category);
        request.setStatus(ExtraCreditRequestStatus.EVIDENCE_SUBMITTED);

        extraCreditRequestRepository.save(request);

        List<ExtraCreditRequest> result = extraCreditRequestRepository.findByStatus(ExtraCreditRequestStatus.EVIDENCE_SUBMITTED);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getStatus()).isEqualTo(ExtraCreditRequestStatus.EVIDENCE_SUBMITTED);
    }

    @Test
    public void testFindByStudent_StudentIdAndStatus() {

        User student = createTestStudent();
        userRepository.save(student);

        Course course = createTestCourse();
        courseRepository.save(course);

        Category category = createTestCategory();
        categoryRepository.save(category);

        ExtraCreditRequest request = new ExtraCreditRequest();
        request.setDescription("Test request");
        request.setStudent(student);
        request.setCourse(course);
        request.setCategory(category);
        request.setStatus(ExtraCreditRequestStatus.EVIDENCE_SUBMITTED);

        extraCreditRequestRepository.save(request);

        List<ExtraCreditRequest> result = 
            extraCreditRequestRepository
                .findByStudent_StudentIdAndStatus(student.getStudentId()
                , ExtraCreditRequestStatus.EVIDENCE_SUBMITTED);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getStudent().getStudentId()).isEqualTo(student.getStudentId());
        assertThat(result.get(0).getStatus()).isEqualTo(ExtraCreditRequestStatus.EVIDENCE_SUBMITTED);
    }

    //@Test
    //public void testFindByChair_IdAndStatus()
}
