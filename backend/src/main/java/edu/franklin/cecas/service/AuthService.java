package edu.franklin.cecas.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import edu.franklin.cecas.domain.User;
import edu.franklin.cecas.domain.UserRole;
import edu.franklin.cecas.dto.CurrentUserResponse;
import edu.franklin.cecas.dto.RegisterRequest;
import edu.franklin.cecas.exception.EmailAlreadyExistsException;
import edu.franklin.cecas.exception.RegistrationNotAllowedException;
import edu.franklin.cecas.repository.UserRepository;

@Service
@Transactional
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordService passwordService;

    public AuthService(UserRepository userRepository, PasswordService passwordService) {
        this.userRepository = userRepository;
        this.passwordService = passwordService;
    }

    public CurrentUserResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new EmailAlreadyExistsException("An account with this email already exists.");
        }

        if (request.getRole() != UserRole.STUDENT) {
            throw new RegistrationNotAllowedException("Only student self registration is allowed.");
        }

        User user = new User();
        user.setEmail(request.getEmail());
        user.setFullName(request.getFullName());
        user.setProgram(request.getProgram());
        user.setRole(request.getRole());
        user.setPassword(passwordService.encode(request.getPassword()));
        user.setStudentId(null);
        user.setIsActive(true);
        user.setMustChangePassword(false);
        user.setEmailVerified(false);

        User savedUser = userRepository.save(user);

        return new CurrentUserResponse(false, savedUser.getEmail(), savedUser.getRole().name());
    }
}
