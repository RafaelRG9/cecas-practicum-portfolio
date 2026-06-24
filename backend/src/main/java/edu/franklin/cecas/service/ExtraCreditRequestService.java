package edu.franklin.cecas.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import edu.franklin.cecas.domain.Category;
import edu.franklin.cecas.domain.Course;
import edu.franklin.cecas.domain.ExtraCreditRequest;
import edu.franklin.cecas.domain.ExtraCreditRequestStatus;
import edu.franklin.cecas.domain.User;
import edu.franklin.cecas.domain.UserRole;
import edu.franklin.cecas.dto.ExtraCreditRequestCreateDTO;
import edu.franklin.cecas.dto.ExtraCreditResponseDTO;
import edu.franklin.cecas.repository.CategoryRepository;
import edu.franklin.cecas.repository.CourseRepository;
import edu.franklin.cecas.repository.ExtraCreditRequestRepository;
import edu.franklin.cecas.repository.UserRepository;
import jakarta.transaction.Transactional;

@Service
@Transactional
public class ExtraCreditRequestService {
    private final ExtraCreditRequestRepository requestRepository;
    private final CourseRepository courseRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;

    public ExtraCreditRequestService(
        ExtraCreditRequestRepository requestRepository,
        CourseRepository courseRepository,
        CategoryRepository categoryRepository,
        UserRepository userRepository) {
            this.requestRepository = requestRepository;
            this.courseRepository = courseRepository;
            this.categoryRepository = categoryRepository;
            this.userRepository = userRepository;
        }

        //Create and persist a new Extra Credit Request.
        public ExtraCreditResponseDTO createRequest(String studentEmail, ExtraCreditRequestCreateDTO dto) {
            //Fetch and validate that the relation IDs actually exist in the database
            Course course = courseRepository.findById(dto.getCourseId())
                .orElseThrow(() -> new RuntimeException("Course not found with ID: " + dto.getCourseId()));

            Category category = categoryRepository.findById(dto.getCategoryId())
                .orElseThrow(() -> new RuntimeException("Category not found with ID: " + dto.getCategoryId()));

            User student = userRepository.findByEmailIgnoreCase(studentEmail)
                .orElseThrow(() -> new RuntimeException("Student not found with Email: " + studentEmail));
            if (student.getRole() != UserRole.STUDENT) {
                throw new RuntimeException("Unauthorized: User is not a student");
            }

            
            
            //Map fields to a brand new domain Entity
            ExtraCreditRequest request = new ExtraCreditRequest();
            request.setCourse(course);
            request.setCategory(category);
            request.setStudent(student);
            request.setDescription(dto.getDescription());

            //Enforce Acceptance Criteria: Default the status to PENDING
            request.setStatus(ExtraCreditRequestStatus.PENDING);

            //Save to database and wrap the entity into the output Response DTO
            ExtraCreditRequest savedRequest = requestRepository.save(request);
            return new ExtraCreditResponseDTO(savedRequest);
        }

        //Get a list of all requests
        public List<ExtraCreditResponseDTO> getRequestsForStudent(String studentEmail) {  
        User student = userRepository.findByEmailIgnoreCase(studentEmail)  
            .orElseThrow(() -> new RuntimeException("User not found"));  

        return requestRepository.findByStudent_Id(student.getId()).stream()  
            .map(ExtraCreditResponseDTO::new)  
            .collect(Collectors.toList());
        }

        public List<ExtraCreditResponseDTO> getAllRequests() {
            return requestRepository.findAll()
                .stream()
                .map(request -> new ExtraCreditResponseDTO(request))
                //.map(ExtraCreditResponseDTO::new)
                .toList();
}
}
