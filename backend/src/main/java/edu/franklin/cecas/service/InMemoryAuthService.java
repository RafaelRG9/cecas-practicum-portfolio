package edu.franklin.cecas.service;

import edu.franklin.cecas.domain.FakeUser;
import edu.franklin.cecas.dto.AuthResponse;
import edu.franklin.cecas.dto.LoginRequest;
import edu.franklin.cecas.dto.RegisterRequest;
import edu.franklin.cecas.repository.FakeUserRepository;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Service
@Profile("auth-dev")
public class InMemoryAuthService implements AuthService {

    private final FakeUserRepository repository;

    public InMemoryAuthService(FakeUserRepository repository) {
        this.repository = repository;
    }

    @Override
    public AuthResponse register(RegisterRequest request) {

        FakeUser user = new FakeUser(
                1L,
                request.username(),
                request.email(),
                request.password()
        );

        repository.save(user);

        return new AuthResponse(
                user.id(),
                user.username(),
                user.email()
        );
    }

    @Override
    public AuthResponse login(LoginRequest request) {

        return repository
                .findByUsername(request.username())
                .map(u -> new AuthResponse(
                        u.id(),
                        u.username(),
                        u.email()))
                .orElseThrow(() ->
                        new IllegalArgumentException("Invalid username"));
    }
}
