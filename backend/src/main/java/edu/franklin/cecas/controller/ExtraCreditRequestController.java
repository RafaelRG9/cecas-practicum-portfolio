package edu.franklin.cecas.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import edu.franklin.cecas.dto.ExtraCreditRequestCreateDTO;
import edu.franklin.cecas.dto.ExtraCreditResponseDTO;
import edu.franklin.cecas.service.ExtraCreditRequestService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/extra-credit-requests")
public class ExtraCreditRequestController {
    
    private final ExtraCreditRequestService extraCreditRequestService;

    public ExtraCreditRequestController(ExtraCreditRequestService extraCreditRequestService) {
        this.extraCreditRequestService = extraCreditRequestService;
    }

    @PreAuthorize("hasRole('STUDENT')")
    @PostMapping
    public ResponseEntity<ExtraCreditResponseDTO> createRequest(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody ExtraCreditRequestCreateDTO dto
    ) {
        ExtraCreditResponseDTO response = 
                extraCreditRequestService.createRequest(userDetails.getUsername(), dto);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
