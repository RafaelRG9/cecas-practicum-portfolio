package edu.franklin.cecas.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import edu.franklin.cecas.domain.User;
import edu.franklin.cecas.dto.ChangePasswordRequest;
import edu.franklin.cecas.dto.UserDTO;
import edu.franklin.cecas.dto.UserProfileResponse;
import edu.franklin.cecas.repository.UserRepository;
import jakarta.transaction.Transactional;

@Service
@Transactional
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    // private final PointCapService pointCapService;
    // private final ExtraCreditRequestService extraCreditRequestService;

    UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        // this.pointCapService = pointCapService;
        // this.extraCreditRequestService = extraCreditRequestService;
    }
    
    public UserDTO getStudentByStudentId(Integer studentId) {
        return userRepository.findByStudentId(studentId)
                .map(UserDTO::new)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }
    /**
     * Get user profile information for the currently authenticated user.
     * @param email
     * @return
     */
    public UserProfileResponse getUserProfile(String email) {
        return userRepository.findByEmailIgnoreCase(email)
                .map(user -> new UserProfileResponse(
                        user.getEmail(),
                        user.getFullName(),
                        user.getRole().name()
                ))
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    /**
     * Change password for user and clear mustChangePassword flag for seeded chairs.
     * @param email
     * @param request
     * 
     */
    public void changePassword(String email, ChangePasswordRequest request) {
        User user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // If user is NOT forced to change password, verify current password.
        if (!Boolean.TRUE.equals(user.getMustChangePassword())) {
            if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
                throw new RuntimeException("Current password is incorrect");
            }
        }

        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new RuntimeException("New password and confirm password do not match");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        // Clear the must-change-password flag (important for seeded Chairs)
        user.setMustChangePassword(false);
        userRepository.save(user);
    }

    /**
     * This is for Program Chair Users
     * 
     */
    public boolean isMustChangePassword(String email) {
        return userRepository.findByEmailIgnoreCase(email)
                .map(user -> Boolean.TRUE.equals(user.getMustChangePassword()))
                .orElse(false);
    }
}
