package org.godsendjoseph.pet_app.models;

/**
 * Model class representing an expense category in the Personal Expense Tracker application.
 */
public class Category {
    private int id;
    private String name;
    private String description;
    private String color; // Hex color code for UI representation
    private int userId; // For user-defined categories, null/0 for default categories

    // Default constructor
    public Category() {
    }

    // Constructor with all fields
    public Category(int id, String name, String description, String color, int userId) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.color = color;
        this.userId = userId;
    }

    // Constructor without id for new category creation
    public Category(String name, String description, String color, int userId) {
        this.name = name;
        this.description = description;
        this.color = color;
        this.userId = userId;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    @Override
    public String toString() {
        return "Category{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", color='" + color + '\'' +
                ", userId=" + userId +
                '}';
    }
}
