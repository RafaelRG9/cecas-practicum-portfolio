package edu.franklin.cecas.dto;

import edu.franklin.cecas.domain.UserRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;


/**
 * This is for user registration.
 */
public class RegisterRequest {
    
    @Email
    @NotBlank
    public String email;

    @Size(min = 8, max = 255)
    @NotBlank
    public String password;

    @Size(max = 100)
    @NotBlank
    public String fullName;

    @Size(max = 45)
    @NotBlank
    public String program;

    @NotNull
    public UserRole role;

    public RegisterRequest() {
    }

    public RegisterRequest(String email, String password, String fullName,
        String program, UserRole role) {
            this.email = email;
            this.password = password;
            this.fullName = fullName;
            this.program = program;
            this.role = role;
    }

    public String getEmail() {
        return this.email;
    }  

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return this.password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getFullName() {
        return this.fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getProgram() {
        return this.program;
    }

    public void setProgram(String program) {
        this.program = program;
    }

    public UserRole getRole() {
        return this.role;
    }

    public void setRole(UserRole role) {
        this.role = role;
    }
}
