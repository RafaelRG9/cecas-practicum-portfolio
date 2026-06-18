package edu.franklin.cecas.web;


import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import edu.franklin.cecas.dto.ChangePasswordRequest;
import edu.franklin.cecas.dto.UserDTO;
import edu.franklin.cecas.dto.UserProfileResponse;
import edu.franklin.cecas.service.UserService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }


    /**
     * Get student by ID - only accessible by chairs
     * @param id student ID
     * @return full user details
     */
    @PreAuthorize("hasRole('CHAIR')")
    @GetMapping("/{studentId}")
    public ResponseEntity<?> getStudentById(@PathVariable Integer studentId) {
        try {
            UserDTO dto = userService.getStudentByStudentId(studentId);
            return ResponseEntity.ok(dto);
        } catch (RuntimeException e) {
            if ("User not found".equals(e.getMessage())) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
            }
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error: " + e.getMessage());
        }
    }

    /**
     * Get the profile of the currently authenticated user
     * @param userDetails
     * @return UserProfileResponse with user details
     */
    @PreAuthorize("hasRole('STUDENT') or hasRole('CHAIR')")
    @GetMapping("/me")
    public UserProfileResponse getUserProfile(@AuthenticationPrincipal UserDetails userDetails) {
         return userService.getUserProfile(userDetails.getUsername());
    }

    /**
     * Change password for the currently authenticated user
     * @param userDetails
     * @param request ChangePasswordRequest with current and new password
     * @return Success message or error
     */
    @PreAuthorize("hasRole('STUDENT') or hasRole('CHAIR')")
    @PostMapping("/change-password")
    public ResponseEntity<?> changePassword(@AuthenticationPrincipal UserDetails userDetails,
                                            @Valid @RequestBody ChangePasswordRequest request) {
        try {
            String email = userDetails.getUsername();
            userService.changePassword(email, request);
            return ResponseEntity.ok("Password changed successfully");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * Must change password for initial CHAIR accounts - this endpoint can be used by seeded chairs to set their password on first login
     * @param userDetails
     * @param request ChangePasswordRequest with new password (current password is not required for forced change)
     * @return Success message or error
     * 
     */
    @PreAuthorize("hasRole('CHAIR')")
    @PostMapping("/force-change-password")
    public ResponseEntity<?> forceChangePassword(@AuthenticationPrincipal UserDetails userDetails,
                                                 @Valid @RequestBody ChangePasswordRequest request) {
        try {
            String email = userDetails.getUsername();
            boolean mustChange = userService.isMustChangePassword(email);
            userService.changePassword(email, request);
            return ResponseEntity.ok(mustChange ? "Password changed successfully. mustChangePassword flag cleared." : "Password changed successfully");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
