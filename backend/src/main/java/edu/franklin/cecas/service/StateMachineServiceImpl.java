package edu.franklin.cecas.service;

import org.springframework.stereotype.Service;

import edu.franklin.cecas.domain.ExtraCreditRequest;
import edu.franklin.cecas.domain.ExtraCreditRequestStatus;
import edu.franklin.cecas.domain.User;
import edu.franklin.cecas.domain.UserRole;
import edu.franklin.cecas.repository.ExtraCreditRequestRepository;
import jakarta.transaction.Transactional;

@Service
@Transactional
public class StateMachineServiceImpl implements StateMachineService {
    
    private final ExtraCreditRequestRepository requestRepository;

    public StateMachineServiceImpl(ExtraCreditRequestRepository requestRepository) {
        this.requestRepository = requestRepository;
    }

    @Override
    public ExtraCreditRequest preApproveRequest(Integer requestId, User actor) {
        ExtraCreditRequest request = requestRepository.findById(requestId)
            .orElseThrow(() -> new RuntimeException("Request not found with ID: " + requestId));

        if (request.getStatus() != ExtraCreditRequestStatus.PENDING) {
            throw new RuntimeException("Invalid transition: Request must be PENDING to be pre-approved.");
        }

        if (actor.getRole() != UserRole.CHAIR) {
            throw new RuntimeException("Unauthorized: Only a Chair can pre-approve requests.");
        }

        request.setStatus(ExtraCreditRequestStatus.PRE_APPROVED);
        return requestRepository.save(request);
    }

    @Override
    public ExtraCreditRequest rejectRequest(Integer requestId, String feedback, User actor) {
        ExtraCreditRequest request = requestRepository.findById(requestId)
            .orElseThrow(() -> new RuntimeException("Request not found with ID: " + requestId));

        if (request.getStatus() != ExtraCreditRequestStatus.PENDING && request.getStatus() != ExtraCreditRequestStatus.EVIDENCE_SUBMITTED) {
            throw new RuntimeException("Invalid status transition.");
        }

        if (actor.getRole() != UserRole.CHAIR) {
            throw new RuntimeException("Unauthorized: Only a Chair can reject requests.");
        }
        
        request.setChairFeedback(feedback);
        request.setStatus(ExtraCreditRequestStatus.REJECTED);
        return requestRepository.save(request);
    }

    @Override
    public ExtraCreditRequest submitEvidenceRequest(Integer requestId, User actor) {
        ExtraCreditRequest request = requestRepository.findById(requestId)
            .orElseThrow(() -> new RuntimeException("Request not found with ID: " + requestId));

        if (request.getStatus() != ExtraCreditRequestStatus.PRE_APPROVED) {
            throw new RuntimeException("Invalid status transition.");
        }

        if (actor.getRole() != UserRole.STUDENT) {
            throw new RuntimeException("Unauthorized: Only a Student can submit evidence.");
        }
        
        request.setStatus(ExtraCreditRequestStatus.EVIDENCE_SUBMITTED);
        return requestRepository.save(request);
    }

    @Override
    public ExtraCreditRequest passDeadlineRequest(Integer requestId) {
        ExtraCreditRequest request = requestRepository.findById(requestId)
            .orElseThrow(() -> new RuntimeException("Request not found with ID: " + requestId));

        if (request.getStatus() != ExtraCreditRequestStatus.PRE_APPROVED) {
            throw new RuntimeException("Invalid status transition.");
        }
        
        request.setStatus(ExtraCreditRequestStatus.CLOSED);
        return requestRepository.save(request);
        }

    @Override
    public ExtraCreditRequest approveWithPointsRequest(Integer requestId, Integer points, User actor) {
        ExtraCreditRequest request = requestRepository.findById(requestId)
            .orElseThrow(() -> new RuntimeException("Request not found with ID: " + requestId));

        if (request.getStatus() != ExtraCreditRequestStatus.EVIDENCE_SUBMITTED) {
            throw new RuntimeException("Invalid status transition.");
        }

        if (actor.getRole() != UserRole.CHAIR) {
            throw new RuntimeException("Unauthorized: Only a Chair can approve requests.");
        }
        
        request.setAwardedPoints(points);
        request.setStatus(ExtraCreditRequestStatus.APPROVED);
        return requestRepository.save(request);
    }

    @Override
    public ExtraCreditRequest resubmitRequest(Integer requestId, User actor) {
        ExtraCreditRequest request = requestRepository.findById(requestId)
            .orElseThrow(() -> new RuntimeException("Request not found with ID: " + requestId));

        if (request.getStatus() != ExtraCreditRequestStatus.REJECTED) {
            throw new RuntimeException("Invalid status transition.");
        }

        if (actor.getRole() != UserRole.STUDENT) {
            throw new RuntimeException("Unauthorized User");
        }
        
        request.setStatus(ExtraCreditRequestStatus.PENDING);
        return requestRepository.save(request);
    }
}