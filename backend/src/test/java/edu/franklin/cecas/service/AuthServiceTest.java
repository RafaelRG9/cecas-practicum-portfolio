package edu.franklin.cecas.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.AfterEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;

import edu.franklin.cecas.support.MySqlServiceTest;
import jakarta.servlet.http.HttpSession;
import edu.franklin.cecas.domain.User;
import edu.franklin.cecas.domain.UserRole;
import edu.franklin.cecas.dto.CurrentUserResponse;
import edu.franklin.cecas.dto.LoginRequest;
import edu.franklin.cecas.dto.RegisterRequest;
import edu.franklin.cecas.exception.EmailAlreadyExistsException;
import edu.franklin.cecas.exception.InvalidCredentialsException;
import edu.franklin.cecas.repository.UserRepository;

@MySqlServiceTest
public class AuthServiceTest {

    @Autowired
    private AuthService authService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    private RegisterRequest createRegisterRequest() {
        return new RegisterRequest(
                "derek@derek.com",
                "TestPass1!",
                "Derek Test",
                "Computer Science",
                1234);
    }

    private LoginRequest createLoginRequest() {
        return new LoginRequest("derek@derek.com", "TestPass1!");
    }

    private User createAndSaveStudent(String email, String password) {
        User user = new User();
        user.setEmail(email);
        user.setFullName("Derek Test");
        user.setPassword(passwordEncoder.encode(password));
        user.setProgram("Computer Science");
        user.setRole(UserRole.STUDENT);
        user.setStudentId(1234);
        user.setIsActive(true);
        user.setMustChangePassword(false);
        user.setEmailVerified(false);
        return userRepository.save(user);
    }

    @Test
    public void testRegisterPersistsNormalizedStudentUserAndDefaultFields() {
        RegisterRequest request = createRegisterRequest();
        request.setEmail("  Derek@Derek.com  ");

        CurrentUserResponse response = authService.register(request);

        User savedUser = userRepository.findByEmailIgnoreCase("derek@derek.com").orElseThrow();

        assertNotNull(savedUser);
        assertEquals("derek@derek.com", savedUser.getEmail());
        assertEquals(request.getFullName(), savedUser.getFullName());
        assertEquals(request.getProgram(), savedUser.getProgram());
        assertEquals(request.getStudentId(), savedUser.getStudentId());
        assertEquals(UserRole.STUDENT, savedUser.getRole());

        assertTrue(passwordEncoder.matches(request.getPassword(), savedUser.getPassword()));
        assertNotEquals(request.getPassword(), savedUser.getPassword());

        assertTrue(Boolean.TRUE.equals(savedUser.getIsActive()));
        assertFalse(Boolean.TRUE.equals(savedUser.getMustChangePassword()));
        assertFalse(Boolean.TRUE.equals(savedUser.getEmailVerified()));

        assertNotNull(response);
        assertFalse(response.authenticated());
        assertEquals(savedUser.getEmail(), response.email());
        assertEquals(savedUser.getRole().name(), response.role());
    }

    @Test
    void testRegisterNormalizesEmailInResponseAndStorage() {
        RegisterRequest request = createRegisterRequest();
        request.setEmail("  Derek@Test.com  ");

        CurrentUserResponse response = authService.register(request);

        User savedUser = userRepository.findByEmailIgnoreCase("derek@test.com").orElseThrow();

        assertEquals("derek@test.com", savedUser.getEmail());
        assertEquals("derek@test.com", response.email());
        assertEquals(Integer.valueOf(1234), savedUser.getStudentId());
        assertEquals(UserRole.STUDENT.name(), response.role());
    }

    @Test
    void testRegisterThrowsWhenEmailAlreadyExistsIgnoringCase() {
        RegisterRequest request = createRegisterRequest();
        request.setEmail("  Derek@Test.com  ");

        createAndSaveStudent("derek@test.com", "Existingpass1!");

        EmailAlreadyExistsException ex = assertThrows(
                EmailAlreadyExistsException.class,
                () -> authService.register(request));

        assertEquals("An account with this email already exists.", ex.getMessage());
    }

    @Test
    void testLoginAuthenticatesUserAndStoresSecurityContext() {
        User user = createAndSaveStudent("derek@derek.com", "TestPass1!");

        LoginRequest request = createLoginRequest();
        MockHttpServletRequest httpRequest = new MockHttpServletRequest();
        MockHttpServletResponse httpResponse = new MockHttpServletResponse();

        CurrentUserResponse response = authService.login(request, httpRequest, httpResponse);

        assertNotNull(response);
        assertTrue(response.authenticated());
        assertEquals(user.getEmail(), response.email());
        assertEquals(user.getRole().name(), response.role());

        HttpSession session = httpRequest.getSession(false);
        assertNotNull(session);
        assertNotNull(session.getAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY));
    }

    @Test
    void testLoginThrowsWhenCredentialsAreInvalid() {
        createAndSaveStudent("derek@derek.com", "TestPass1!");

        LoginRequest request = new LoginRequest("derek@derek.com", "WrongPass1!");
        MockHttpServletRequest httpRequest = new MockHttpServletRequest();
        MockHttpServletResponse httpResponse = new MockHttpServletResponse();

        InvalidCredentialsException ex = assertThrows(
                InvalidCredentialsException.class,
                () -> authService.login(request, httpRequest, httpResponse));

        assertEquals("Invalid email or password.", ex.getMessage());
        assertNull(httpRequest.getSession(false));
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }
}
