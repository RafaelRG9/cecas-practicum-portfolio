package edu.franklin.cecas.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;

import edu.franklin.cecas.support.MySqlServiceTest;
import edu.franklin.cecas.domain.User;
import edu.franklin.cecas.domain.UserRole;
import edu.franklin.cecas.dto.CurrentUserResponse;
import edu.franklin.cecas.dto.RegisterRequest;
import edu.franklin.cecas.exception.EmailAlreadyExistsException;
import edu.franklin.cecas.exception.RegistrationNotAllowedException;
import edu.franklin.cecas.repository.UserRepository;

@MySqlServiceTest
public class AuthServiceTest {

    @Autowired
    private AuthService authService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private RegisterRequest createRegisterRequest() {
        return new RegisterRequest(
            "derek@derek.com",
            "TestPass1!",
            "Derek Test",
            "Computer Science",
            UserRole.STUDENT
        );
    }

    @Test
    public void registerPersistsEncodedPasswordAndDefaultFlags() {
        RegisterRequest request = createRegisterRequest();

        CurrentUserResponse response = authService.register(request);

        User savedUser = userRepository.findByEmail(request.getEmail()).orElseThrow();

        assertNotNull(savedUser);
        assertEquals(request.getEmail(), savedUser.getEmail());
        assertEquals(request.getFullName(), savedUser.getFullName());
        assertEquals(request.getProgram(), savedUser.getProgram());
        assertEquals(UserRole.STUDENT, savedUser.getRole());

        assertTrue(passwordEncoder.matches(request.getPassword(), savedUser.getPassword()));
        assertNotEquals(request.getPassword(), savedUser.getPassword());

        assertNull(savedUser.getStudentId());
        assertTrue(Boolean.TRUE.equals(savedUser.getIsActive()));
        assertFalse(Boolean.TRUE.equals(savedUser.getMustChangePassword()));
        assertFalse(Boolean.TRUE.equals(savedUser.getEmailVerified()));

        assertNotNull(response);
        assertFalse(response.authenticated());
        assertEquals(savedUser.getEmail(), response.email());
        assertEquals(savedUser.getRole().name(), response.role());
    }

    @Test
    void registerThrowsWhenEmailAlreadyExists() {
        RegisterRequest request = createRegisterRequest();

        User existingUser = new User();
        existingUser.setEmail(request.getEmail());
        existingUser.setFullName("Existing User");
        existingUser.setPassword(passwordEncoder.encode("Existingpass1!"));
        existingUser.setProgram("Computer Science");
        existingUser.setRole(UserRole.STUDENT);
        existingUser.setStudentId(null);
        existingUser.setIsActive(true);
        existingUser.setMustChangePassword(false);
        existingUser.setEmailVerified(false);
        userRepository.save(existingUser);

        EmailAlreadyExistsException ex = assertThrows(
                EmailAlreadyExistsException.class,
                () -> authService.register(request)
        );

        assertEquals("An account with this email already exists.", ex.getMessage());
    }

    @Test
    void registerThrowsWhenRoleIsChair() {
        RegisterRequest request = createRegisterRequest();
        request.setRole(UserRole.CHAIR);

        RegistrationNotAllowedException ex = assertThrows(
                RegistrationNotAllowedException.class,
                () -> authService.register(request)
        );

        assertEquals("Only student self registration is allowed.", ex.getMessage());
    }
}
