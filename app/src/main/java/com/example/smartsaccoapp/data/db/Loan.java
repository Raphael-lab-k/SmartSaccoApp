package com.example.smartsaccoapp.data.db;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "loans")
public class Loan {
    @PrimaryKey(autoGenerate = true)
    public int id;
    public String memberEmail; // Link to member
    public String type;
    public double amount;
    public double interestRate;
    public double repaidAmount; // Track progress
    public String status; // PENDING, APPROVED, REJECTED, PAID, OVERDUE
    public String purpose;
    public String riskLevel; // LOW, MEDIUM, HIGH
    public long dueDate;

    public Loan() {} // Required for Firestore

    public Loan(String memberEmail, String type, double amount, double interestRate, String status, String purpose, long dueDate) {
        this.memberEmail = memberEmail;
        this.type = type;
        this.amount = amount;
        this.interestRate = interestRate;
        this.status = status;
        this.purpose = purpose;
        this.dueDate = dueDate;
        this.repaidAmount = 0.0;
        this.riskLevel = "LOW"; // Default
    }
}