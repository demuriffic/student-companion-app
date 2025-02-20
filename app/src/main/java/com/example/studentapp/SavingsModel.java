package com.example.studentapp;

public class SavingsModel {

    private int id;
    private float amount;
    private String date;

    // constructors
    public SavingsModel(int id, float amount, String date) {
        this.id = id;
        this.amount = amount;
        this.date = date;
    }
    // toString
    @Override
    public String toString() {
        return "AMOUNT: " + amount +
                ", DATE: " + date + '\n';
    }

    // getters and setters
    public SavingsModel() {

    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public float getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }
}
