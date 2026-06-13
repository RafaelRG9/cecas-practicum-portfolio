package edu.franklin.cecas.web;

import edu.franklin.cecas.dto.AuthResponse;
import edu.franklin.cecas.dto.LoginRequest;
import edu.franklin.cecas.dto.RegisterRequest;
import edu.franklin.cecas.service.AuthService;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import edu.franklin.cecas.dto.CurrentUserResponse;

@RestController
public class AuthController {

    private final AuthService authService;

    //private AuthenticationManager authenticationManager;
    //private UserDetailsService userDetailsService;
    //private PasswordEncoder passwordEncoder;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @GetMapping("/api/auth/me")
    public CurrentUserResponse getCurrentUser(Authentication authentication) {
        if (authentication == null || authentication instanceof AnonymousAuthenticationToken) {
            return new CurrentUserResponse(false, null, null);
        }
        
        String email = authentication.getName();

        String role = authentication.getAuthorities().stream()
            .map(GrantedAuthority::getAuthority)
            .filter(authority -> authority.startsWith("ROLE_"))
            .map(authority -> authority.substring("ROLE_".length()))
            .findFirst()
            .orElse(null);
        
        return new CurrentUserResponse(true, email, role);
    }

    @PostMapping("/api/auth/login")
    public AuthResponse login(
            @RequestBody LoginRequest request) {
        return authService.login(request);
    }

    @PostMapping("/api/auth/register")
    public AuthResponse register(
            @RequestBody RegisterRequest request) {
        return authService.register(request);
    }
}
