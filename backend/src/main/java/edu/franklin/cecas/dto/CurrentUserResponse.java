package edu.franklin.cecas.dto;

public record CurrentUserResponse(
    boolean authenticated,
    String email,
    String role
) {}