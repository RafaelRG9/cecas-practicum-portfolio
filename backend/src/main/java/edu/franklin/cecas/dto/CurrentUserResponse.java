package edu.franklin.cecas.dto;

//import java.util.List;

public record CurrentUserResponse(
    boolean authenticated,
    String email,
    String role
) {
}