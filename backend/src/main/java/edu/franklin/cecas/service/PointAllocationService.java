package edu.franklin.cecas.exception;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

import edu.franklin.cecas.domain.ExtraCreditRequest;
import edu.franklin.cecas.domain.ExtraCreditRequestStatus;
import edu.franklin.cecas.repository.ExtraCreditRequestRepository;

@Service
public class PointAllocationService {
    
    private static final int MAX_POINTS = 50;
    
    private final ExtraCreditRequestRepository requestRepository;

    public PointAllocationService(ExtraCreditRequestRepository requestRepository) {
        this.requestRepository = requestRepository;
    }

    public int getUsedPoints(Integer studentId) {
        List<ExtraCreditRequest> approvedRequests = requestRepository.findByStudent_StudentIdAndStatus(studentId, ExtraCreditRequestStatus.APPROVED);

        int usedPoints = 0;

        for (ExtraCreditRequest request : approvedRequests) {
            if (request.getAwardedPoints() != null) {
                usedPoints += request.getAwardedPoints();
            }
        }

        return usedPoints;
    }

    public int getRemainingPoints(Integer studentId) {
        return MAX_POINTS - getUsedPoints(studentId);
    }

    public boolean canAwardPoints(Integer studentId, int requestedPoints) {
        return getUsedPoints(studentId) + requestedPoints <= MAX_POINTS;
    }

    @Transactional
    public void validatePointAllocation(Integer studentId, int requestedPoints) {
        int usedPoints = getUsedPoints(studentId);

        if (usedPoints + requestedPoints > MAX_POINTS) {
            throw new PointCapExceededException("Student exceeds 50 point maximum.");
        }
    }
}