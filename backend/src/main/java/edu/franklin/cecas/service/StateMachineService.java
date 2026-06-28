package edu.franklin.cecas.service;

import edu.franklin.cecas.domain.ExtraCreditRequest;
import edu.franklin.cecas.domain.User;

public interface StateMachineService {
    // PENDING -> PRE_APPROVED (Chair Only)
    ExtraCreditRequest preApproveRequest(Integer requestId, User actor);
    
    // PENDING or EVIDENCE_SUBMITTED -> REJECTED (Chair Only)
    ExtraCreditRequest rejectRequest(Integer requestId, String feedback, User actor);
    
    // PRE_APPROVED -> EVIDENCE_SUBMITTED (Student Only)
    ExtraCreditRequest submitEvidenceRequest(Integer requestId, User actor);
    
    // PRE_APPROVED -> CLOSED (System/Automated)
    ExtraCreditRequest passDeadlineRequest(Integer requestId);
    
    // EVIDENCE_SUBMITTED -> APPROVED (Chair Only)
    ExtraCreditRequest approveWithPointsRequest(Integer requestId, Integer points, User actor);
    
    // REJECTED -> PENDING (Student Only)
    ExtraCreditRequest resubmitRequest(Integer requestId, User actor);

}
