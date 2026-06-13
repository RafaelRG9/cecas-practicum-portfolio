package edu.franklin.cecas.dto;

public record AuthResponse(
        Long id,
        String username,
        String email
) {}
