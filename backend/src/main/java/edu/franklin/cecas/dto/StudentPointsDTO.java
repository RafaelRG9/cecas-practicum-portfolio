package edu.franklin.cecas.dto;

/**
 * This is for the student points summary, which includes issued, pending, and available points.
 */
public class StudentPointsDTO {
    private int issued;
    private int pending;
    private int available;

    public StudentPointsDTO() {}
    public StudentPointsDTO(int issued, int pending, int available) { 
        this.issued=issued; 
        this.pending=pending; 
        this.available=available; 
    }
    public int getIssued() {
        return issued;
    }
    public void setIssued(int issued) {
        this.issued = issued;
    }
    public int getPending() {
        return pending;
    }
    public void setPending(int pending) {
        this.pending = pending;
    }
    public int getAvailable() {
        return available;
    }
    public void setAvailable(int available) {
        this.available = available;
    }
}
