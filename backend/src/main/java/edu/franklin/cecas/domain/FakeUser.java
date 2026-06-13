package edu.franklin.cecas.domain;


public record FakeUser(
        Long id,
        String username,
        String email,
        String password
) {
}
