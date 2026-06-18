package edu.franklin.cecas.service;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;

import edu.franklin.cecas.domain.User;
import edu.franklin.cecas.domain.UserRole;
import edu.franklin.cecas.dto.ChangePasswordRequest;
import edu.franklin.cecas.dto.UserProfileResponse;
import edu.franklin.cecas.repository.UserRepository;
import edu.franklin.cecas.support.MySqlServiceTest;

@MySqlServiceTest
public class UserServiceTest {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private User createTestUser() {
        User user = new User();

        user.setFullName("Sample User");
        user.setEmail("sample@test.com");
        user.setPassword("password");
        user.setStudentId(1001);
        user.setProgram("Computer Science");
        user.setIsActive(true);
        user.setMustChangePassword(false);

        return user;
    }

    @Test
    void testGetUserProfileReturnsCorrectData() {
        User user = createTestUser();
        user.setRole(UserRole.STUDENT);
        User savedUser = userRepository.save(user);

        UserProfileResponse profile = userService.getUserProfile("sample@test.com");
        assertNotNull(profile);
        assertEquals(savedUser.getEmail(), profile.getEmail());
        assertEquals(savedUser.getFullName(), profile.getFullName());
        assertEquals(savedUser.getRole().name(), profile.getRole());
    }

    @Test
    void testForceChangePassword() {
        User user = createTestUser();
        user.setRole(UserRole.CHAIR);
        user.setMustChangePassword(true);
        user.setPassword(passwordEncoder.encode("initial"));
        userRepository.save(user);

        ChangePasswordRequest req = new ChangePasswordRequest(null, "newPass", "newPass");

        userService.changePassword(user.getEmail(), req);

        User updated = userRepository.findByEmail(user.getEmail()).orElseThrow();
        assertTrue(passwordEncoder.matches("newPass", updated.getPassword()));
        assertFalse(Boolean.TRUE.equals(updated.getMustChangePassword()));
    }

    @Test
    void testChangePassword() {
        String email = "student@example.edu";
        User user = createTestUser();
        user.setRole(UserRole.STUDENT);
        user.setEmail(email);
        user.setMustChangePassword(false);
        user.setPassword(passwordEncoder.encode("current"));
        userRepository.save(user);

        ChangePasswordRequest req = new ChangePasswordRequest("current", "newPass", "newPass");

        userService.changePassword(email, req);

        User updated = userRepository.findByEmail(email).orElseThrow();
        assertTrue(passwordEncoder.matches("newPass", updated.getPassword()));
        assertFalse(Boolean.TRUE.equals(updated.getMustChangePassword()));
    }

    @Test
    void testChangePasswordThrows() {
        User user = createTestUser();
        user.setRole(UserRole.STUDENT);
        user.setMustChangePassword(false);
        user.setPassword(passwordEncoder.encode("correct"));
        userRepository.save(user);

        ChangePasswordRequest req = new ChangePasswordRequest("wrong", "new", "new");

        RuntimeException ex = assertThrows(RuntimeException.class, () -> userService.changePassword(user.getEmail(), req));
        assertEquals("Current password is incorrect", ex.getMessage());
    }

    @Test
    void testChangePasswordNewAndConfirmDontMatchThrows() {
        User user = createTestUser();
        user.setRole(UserRole.STUDENT);
        user.setMustChangePassword(true);
        user.setPassword(passwordEncoder.encode("whatever"));
        userRepository.save(user);

        ChangePasswordRequest req = new ChangePasswordRequest(null, "newA", "newB");

        RuntimeException ex = assertThrows(RuntimeException.class, () -> userService.changePassword(user.getEmail(), req));
        assertEquals("New password and confirm password do not match", ex.getMessage());
    }

    @Test
    void testIsMustChangePasswordReturnsTrueOrFalse() {
        User uTrue = createTestUser();
        uTrue.setRole(UserRole.CHAIR);
        uTrue.setEmail("mustchange@test.com");
        uTrue.setMustChangePassword(true);
        User saved = userRepository.save(uTrue);

        boolean valTrue = userService.isMustChangePassword(saved.getEmail());
        assertTrue(valTrue);

        saved.setMustChangePassword(false);
        userRepository.save(saved);

        boolean valFalse = userService.isMustChangePassword(saved.getEmail());
        assertFalse(valFalse);
    }
}