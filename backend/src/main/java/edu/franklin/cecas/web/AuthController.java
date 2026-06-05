package edu.franklin.cecas.web;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import edu.franklin.cecas.dto.CurrentUserResponse;

@RestController
public class AuthController {
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
}
