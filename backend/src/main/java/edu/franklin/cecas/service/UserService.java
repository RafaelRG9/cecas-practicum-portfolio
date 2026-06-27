package edu.franklin.cecas.service;

import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import edu.franklin.cecas.domain.User;
import edu.franklin.cecas.domain.UserRole;
import edu.franklin.cecas.dto.ChangePasswordRequest;
import edu.franklin.cecas.dto.UserDTO;
import edu.franklin.cecas.dto.UserProfileResponse;
import edu.franklin.cecas.exception.InvalidPasswordException;
import edu.franklin.cecas.exception.PasswordChangeNotRequiredException;
import edu.franklin.cecas.exception.PasswordMismatchException;
import edu.franklin.cecas.exception.UnauthorizedRoleException;
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
     * Normal Change Password logic
     * @param email
     * @param request
     * 
     */
    public void changePassword(String email, ChangePasswordRequest request) {
        User user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new InvalidPasswordException("Current password is incorrect");
        }

        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new PasswordMismatchException("New password and confirm password do not match");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }

    /**
     * Force change password for program chairs and clear mustChangePassword flag.
     * @param email
     * @param request
     */
    public void forceChangePassword(String email, ChangePasswordRequest request) {
        User user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));

        if (user.getRole() != UserRole.CHAIR) {
            throw new UnauthorizedRoleException("Only program chairs can force change their password");
        }
        // Check if mustChangePassword flag is true
        if (!Boolean.TRUE.equals(user.getMustChangePassword())) {
            throw new PasswordChangeNotRequiredException ("Password change is not required");
        }
        // verify current password matches the stored password for the user
        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new InvalidPasswordException("Current password is incorrect");
        }
        // verify new password and confirm password match
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new PasswordMismatchException("New password and confirm password do not match");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        user.setMustChangePassword(false); // Clear the mustChangePassword flag after successful password change

        userRepository.save(user);
    }
}
