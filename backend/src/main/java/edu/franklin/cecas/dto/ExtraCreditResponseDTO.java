package edu.franklin.cecas.dto;

import edu.franklin.cecas.domain.ExtraCreditRequest;
import edu.franklin.cecas.domain.ExtraCreditRequestStatus;
import edu.franklin.cecas.domain.User;
import edu.franklin.cecas.domain.Course;
import edu.franklin.cecas.domain.Category;
import java.time.LocalDateTime;

public class ExtraCreditResponseDTO {
    private Integer id;
    private String description;
    private User student;
    private User chair;
    private Course course;
    private Category category;
    private ExtraCreditRequestStatus status;
    private String evidenceFilePath;
    private LocalDateTime dueDate;
    private Integer awardedPoints;
    private String chairFeedback;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public ExtraCreditResponseDTO() {}

    public ExtraCreditResponseDTO(ExtraCreditRequest extraCreditRequest) {
        this.id = extraCreditRequest.getId();
        this.description = extraCreditRequest.getDescription();
        this.student = extraCreditRequest.getStudent();
        this.chair = extraCreditRequest.getChair();
        this.course = extraCreditRequest.getCourse();
        this.category = extraCreditRequest.getCategory();
        this.status = extraCreditRequest.getStatus();
        this.evidenceFilePath = extraCreditRequest.getEvidenceFilePath();
        this.dueDate = extraCreditRequest.getDueDate();
        this.awardedPoints = extraCreditRequest.getAwardedPoints();
        this.chairFeedback = extraCreditRequest.getChairFeedback();
        this.createdAt = extraCreditRequest.getCreatedAt();
        this.updatedAt = extraCreditRequest.getUpdatedAt();
    }

        public Integer getId() {
            return id;
        }

        public void setId(Integer id) {
            this.id = id;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public User getStudent() {
            return student;
        }

        public void setStudent(User student) {
            this.student = student;
        }

        public User getChair() {
            return chair;
        }

        public void setChair(User chair) {
            this.chair = chair;
        }

        public Course getCourse() {
            return course;
        }

        public void setCourse(Course course) {
            this.course = course;
        }

        public Category getCategory() {
            return category;
        }

        public void setCategory(Category category) {
            this.category = category;
        }

        public ExtraCreditRequestStatus getStatus() {
            return status;
        }

        public void setStatus(ExtraCreditRequestStatus status) {
            this.status = status;
        }

        public String getEvidenceFilePath() {
            return evidenceFilePath;
        }

        public void setEvidenceFilePath(String evidenceFilePath) {
            this.evidenceFilePath = evidenceFilePath;
        }

        public LocalDateTime getDueDate() {
            return dueDate;
        }

        public void setDueDate(LocalDateTime dueDate) {
            this.dueDate = dueDate;
        }

        public Integer getAwardedPoints() {
            return awardedPoints;
        }

        public void setAwardedPoints(Integer awardedPoints) {
            this.awardedPoints = awardedPoints;
        }

        public String getChairFeedback() {
            return chairFeedback;
        }

        public void setChairFeedback(String chairFeedback) {
            this.chairFeedback = chairFeedback;
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