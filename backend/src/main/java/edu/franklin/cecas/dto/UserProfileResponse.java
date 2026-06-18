package edu.franklin.cecas.dto;

/**
 * This will be for the user's dashboard/profile page.'
 *
 */
public class UserProfileResponse {
    private String email;
    private String fullName;
    private String role;

    public UserProfileResponse(String email, String fullName, String role) {
        this.email = email;
        this.fullName = fullName;
        this.role = role;
    }

    public String getEmail() {
        return email;
    }

    public String getFullName() {
        return fullName;
    }


    public String getRole() {
        return role;
    }
}
