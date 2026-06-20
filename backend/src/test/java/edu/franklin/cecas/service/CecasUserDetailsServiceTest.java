package edu.franklin.cecas.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.Mock;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import edu.franklin.cecas.domain.User;
import edu.franklin.cecas.domain.UserRole; 
import edu.franklin.cecas.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
public class CecasUserDetailsServiceTest {
    @Mock
    private UserRepository userRepository;

    private CecasUserDetailsService service;

    @BeforeEach
    void setUp() {
        service = new CecasUserDetailsService(userRepository);
    }

    @Test
    void loadUserByUsername_whenUserExists_returnsUserDetails() {
        User user = mock(User.class);
        when(userRepository.findByEmailIgnoreCase("sample@test.com")).thenReturn(Optional.of(user));
        when(user.getEmail()).thenReturn("sample@test.com");
        when(user.getPassword()).thenReturn("Somepassword!");
        when(user.getIsActive()).thenReturn(true);

        UserRole role = mock(UserRole.class);
        when(role.name()).thenReturn("STUDENT");
        when(user.getRole()).thenReturn(role);

        UserDetails ud = service.loadUserByUsername("sample@test.com");

        assertEquals("sample@test.com", ud.getUsername());
        assertEquals("Somepassword!", ud.getPassword());
        assertTrue(ud.isEnabled());
        assertTrue(ud.getAuthorities().stream()
            .anyMatch(a -> "ROLE_STUDENT".equals(a.getAuthority())));
    }

    @Test
    void loadUserByUsername_whenUserMissing_throws() {
        when(userRepository.findByEmailIgnoreCase("missing@test.com")).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class, () -> {
            service.loadUserByUsername("missing@test.com");
        });
    }
}