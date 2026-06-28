package edu.franklin.cecas.service;

import java.util.Locale;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.session.SessionAuthenticationStrategy;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import edu.franklin.cecas.domain.User;
import edu.franklin.cecas.domain.UserRole;
import edu.franklin.cecas.dto.CurrentUserResponse;
import edu.franklin.cecas.dto.LoginRequest;
import edu.franklin.cecas.dto.RegisterRequest;
import edu.franklin.cecas.exception.EmailAlreadyExistsException;
import edu.franklin.cecas.exception.InvalidCredentialsException;
import edu.franklin.cecas.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Service
@Transactional
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordService passwordService;
    private final AuthenticationManager authenticationManager;
    private final SecurityContextRepository securityContextRepository;
    private final SessionAuthenticationStrategy sessionAuthenticationStrategy;

    public AuthService(UserRepository userRepository,
            PasswordService passwordService,
            AuthenticationManager authenticationManager,
            SecurityContextRepository securityContextRepository,
            SessionAuthenticationStrategy sessionAuthenticationStrategy) {
        this.userRepository = userRepository;
        this.passwordService = passwordService;
        this.authenticationManager = authenticationManager;
        this.securityContextRepository = securityContextRepository;
        this.sessionAuthenticationStrategy = sessionAuthenticationStrategy;
    }

    public CurrentUserResponse register(RegisterRequest request) {

        String normalizedEmail = normalizeEmail(request.getEmail());

        if (userRepository.existsByEmailIgnoreCase(normalizedEmail)) {
            throw new EmailAlreadyExistsException("An account with this email already exists.");
        }

        User user = new User();
        user.setEmail(normalizedEmail);
        user.setFullName(request.getFullName());
        user.setProgram(request.getProgram());
        user.setRole(UserRole.STUDENT);
        user.setPassword(passwordService.encode(request.getPassword()));
        user.setStudentId(request.getStudentId());
        user.setIsActive(true);
        user.setMustChangePassword(false);
        user.setEmailVerified(false);

        User savedUser = userRepository.save(user);

        return new CurrentUserResponse(false, savedUser.getEmail(), savedUser.getRole().name(), false);
    }

    public CurrentUserResponse login(LoginRequest request, HttpServletRequest httpRequest,
            HttpServletResponse httpResponse) {

        String normalizedEmail = normalizeEmail(request.getEmail());

        try {
            UsernamePasswordAuthenticationToken authRequest = new UsernamePasswordAuthenticationToken(
                    normalizedEmail,
                    request.getPassword());

            Authentication authentication = authenticationManager.authenticate(authRequest);

            SecurityContext context = SecurityContextHolder.createEmptyContext();
            context.setAuthentication(authentication);
            SecurityContextHolder.setContext(context);

            sessionAuthenticationStrategy.onAuthentication(authentication, httpRequest, httpResponse);
            securityContextRepository.saveContext(context, httpRequest, httpResponse);

            User user = userRepository.findByEmailIgnoreCase(normalizedEmail)
                .orElseThrow(() -> new InvalidCredentialsException("Invalid email or password."));

            return toCurrentUserResponse(user, true);

        } catch (AuthenticationException ex) {
            SecurityContextHolder.clearContext();
            throw new InvalidCredentialsException("Invalid email or password.");
        }
    }

    public CurrentUserResponse getCurrentUserResponse(Authentication authentication) {
        if (authentication == null
                || authentication instanceof AnonymousAuthenticationToken) {
            return new CurrentUserResponse(false, null, null, false);
        }

        String email = normalizeEmail(authentication.getName());

        User user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new InvalidCredentialsException("Invalid email or password."));

        return toCurrentUserResponse(user, true);
    }

    private String normalizeEmail(String email) {
        return email == null ? null : email.trim().toLowerCase(Locale.ROOT);
    }

    private CurrentUserResponse toCurrentUserResponse(User user, boolean authenticated) {
    return new CurrentUserResponse(
            authenticated,
            user.getEmail(),
            user.getRole().name(),
            Boolean.TRUE.equals(user.getMustChangePassword()));
}
}
