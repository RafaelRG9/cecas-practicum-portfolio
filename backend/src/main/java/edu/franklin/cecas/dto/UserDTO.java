package edu.franklin.cecas.dto;

import java.time.LocalDateTime;

import edu.franklin.cecas.domain.User;
import edu.franklin.cecas.domain.UserRole;

public class UserDTO {
    private Integer id;
    private String email;
    private String fullName;
    private UserRole role;
    private Integer studentId;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

public UserDTO() {}

public UserDTO(User user) {
    this.id = user.getId();
    this.email = user.getEmail();
    this.fullName = user.getFullName();
    this.role = user.getRole();
    this.studentId = user.getStudentId();
    this.isActive = user.getIsActive();
    this.createdAt = user.getCreatedAt();
    this.updatedAt = user.getUpdatedAt();
}

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public UserRole getRole() {
        return role;
    }

    public void setRole(UserRole role) {
        this.role = role;
    }

    public Integer getStudentId() {
        return studentId;
    }

    public void setStudentId(Integer studentId) {
        this.studentId = studentId;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

}
