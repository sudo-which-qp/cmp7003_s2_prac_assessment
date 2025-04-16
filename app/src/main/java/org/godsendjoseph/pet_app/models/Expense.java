package org.godsendjoseph.pet_app.models;

/**
 * Model class representing an expense entry in the Personal Expense Tracker application.
 */
public class Expense {
    private int id;
    private int userId;
    private String title;
    private double amount;
    private String date;
    private String time;
    private String location;
    private int categoryId;
    private String notes;
    private String createdAt;

    // Default constructor
    public Expense() {
    }

    // Constructor with all fields
    public Expense(int id, int userId, String title, double amount, String date, String time,
                   String location, int categoryId, String notes, String createdAt) {
        this.id = id;
        this.userId = userId;
        this.title = title;
        this.amount = amount;
        this.date = date;
        this.time = time;
        this.location = location;
        this.categoryId = categoryId;
        this.notes = notes;
        this.createdAt = createdAt;
    }

    // Constructor without id for new expense creation
    public Expense(int userId, String title, double amount, String date, String time,
                   String location, int categoryId, String notes) {
        this.userId = userId;
        this.title = title;
        this.amount = amount;
        this.date = date;
        this.time = time;
        this.location = location;
        this.categoryId = categoryId;
        this.notes = notes;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public int getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(int categoryId) {
        this.categoryId = categoryId;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return "Expense{" +
                "id=" + id +
                ", userId=" + userId +
                ", title='" + title + '\'' +
                ", amount=" + amount +
                ", date='" + date + '\'' +
                ", time='" + time + '\'' +
                ", location='" + location + '\'' +
                ", categoryId=" + categoryId +
                ", notes='" + notes + '\'' +
                ", createdAt='" + createdAt + '\'' +
                '}';
    }
}