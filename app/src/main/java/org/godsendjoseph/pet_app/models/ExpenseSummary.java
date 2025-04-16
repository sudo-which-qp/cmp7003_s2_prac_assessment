package org.godsendjoseph.pet_app.models;

public class ExpenseSummary {
    private String category;
    private double amount;
    private double percentage;
    private String color;
    private int count;

    /**
     * Default constructor
     */
    public ExpenseSummary() {
    }

    /**
     * Constructor with all fields
     * @param category Category or grouping name
     * @param amount Total amount
     * @param percentage Percentage of total expenses
     * @param color Color for UI representation
     * @param count Number of expenses in this summary
     */
    public ExpenseSummary(String category, double amount, double percentage, String color, int count) {
        this.category = category;
        this.amount = amount;
        this.percentage = percentage;
        this.color = color;
        this.count = count;
    }

    /**
     * Constructor without count
     */
    public ExpenseSummary(String category, double amount, double percentage, String color) {
        this.category = category;
        this.amount = amount;
        this.percentage = percentage;
        this.color = color;
        this.count = 0;
    }

    // Getters and Setters
    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public double getPercentage() {
        return percentage;
    }

    public void setPercentage(double percentage) {
        this.percentage = percentage;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    @Override
    public String toString() {
        return "ExpenseSummary{" +
                "category='" + category + '\'' +
                ", amount=" + amount +
                ", percentage=" + percentage +
                ", color='" + color + '\'' +
                ", count=" + count +
                '}';
    }
}
