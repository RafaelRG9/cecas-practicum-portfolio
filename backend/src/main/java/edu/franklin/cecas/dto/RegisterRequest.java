package edu.franklin.cecas.dto;

public record RegisterRequest(
        String username,
        String email,
        String password
) {
}
