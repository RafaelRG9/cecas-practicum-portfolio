package edu.franklin.cecas.domain;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "categories")

public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "category_id", nullable = false)
    private Integer categoryId;

    @Column(name = "category_name", nullable = false)
    private String categoryName;

    @Column(name = "description", nullable = false, length = 1000)
    private String description;
    
    @Column(name = "default_points", nullable = false)
    private Integer defaultPoints;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    public Category() {

    }

    public Integer getCategoryId() {
        return categoryId;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getDefaultPoints() {
        return defaultPoints;
    }

    public void setDefaultPoints(Integer defaultPoints) {
        this.defaultPoints = defaultPoints;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}