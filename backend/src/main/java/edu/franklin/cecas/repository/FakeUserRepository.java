package edu.franklin.cecas.repository;

import edu.franklin.cecas.domain.FakeUser;
import org.springframework.context.annotation.Profile;

import java.util.Optional;

@Profile("auth-dev")
public interface FakeUserRepository {

    Optional<FakeUser> findByUsername(String username);

    void save(FakeUser user);
}
