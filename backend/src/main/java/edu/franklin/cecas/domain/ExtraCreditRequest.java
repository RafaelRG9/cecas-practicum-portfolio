package edu.franklin.cecas.domain;
import java.time.LocalDateTime;
import jakarta.persistence.*;

@Entity
@Table(name = "extra_credit_requests")
public class ExtraCreditRequest {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Column(name = "description", nullable = false, length = 1000)
    private String description;

    // --- FOREIGN KEY RELATIONSHIPS ---

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private User student;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chair_id")
    private User chair;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    // --- ADDITIONAL COLUMNS ---

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private ExtraCreditRequestStatus status;

    @Column(name = "evidence_file_path", nullable = true, length = 255)
    private String evidenceFilePath;

    @Column(name = "due_date", nullable = true)
    private LocalDateTime dueDate;

    @Column(name = "awarded_points", nullable = true)
    private Integer awardedPoints;

    @Column(name = "chair_feedback", nullable = true, length = 1000)
    private String chairFeedback;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", insertable = false, updatable = false)
    private LocalDateTime updatedAt;



    public ExtraCreditRequest() {

    }

    public Integer getId() {
        return id;
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

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
