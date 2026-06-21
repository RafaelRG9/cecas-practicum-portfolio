package edu.franklin.cecas.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import edu.franklin.cecas.domain.ExtraCreditRequest;
import edu.franklin.cecas.domain.ExtraCreditRequestStatus;

public interface ExtraCreditRequestRepository extends JpaRepository<ExtraCreditRequest, Integer>{
    List<ExtraCreditRequest> findByStudent_Id(Integer userId);

    List<ExtraCreditRequest> findByChair_Id(Integer chairId);

    List<ExtraCreditRequest> findByCourse_CourseId(Integer courseId);

    List<ExtraCreditRequest> findByCategory_CategoryId(Integer categoryId);

    List<ExtraCreditRequest> findByStatus(ExtraCreditRequestStatus status);

    List<ExtraCreditRequest> findByStudent_IdAndStatus(Integer userId, ExtraCreditRequestStatus status);

    List<ExtraCreditRequest> findByChair_IdAndStatus(Integer chairId, ExtraCreditRequestStatus status);
}