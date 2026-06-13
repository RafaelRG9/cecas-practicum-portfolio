package edu.franklin.cecas.repository;

import edu.franklin.cecas.domain.FakeUser;
import org.springframework.stereotype.Repository;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.context.annotation.Profile;

@Repository
@Profile("auth-dev")
public class InMemoryUserRepository implements FakeUserRepository {

    private final Map<String, FakeUser> users = new ConcurrentHashMap<>();

    @Override
    public Optional<FakeUser> findByUsername(String username) {
        return Optional.ofNullable(users.get(username));
    }

    @Override
    public void save(FakeUser user) {
        users.put(user.username(), user);
    }
}
