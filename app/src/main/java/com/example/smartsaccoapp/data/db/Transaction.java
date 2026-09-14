package com.example.smartsaccoapp.data.db;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "transactions")
public class Transaction {
    @PrimaryKey(autoGenerate = true)
    public int id;
    public String type; // DEPOSIT, WITHDRAWAL, LOAN_REPAYMENT
    public double amount;
    public long timestamp;
    public String description;

    public Transaction() {} // Required for Firestore

    public Transaction(String type, double amount, long timestamp, String description) {
        this.type = type;
        this.amount = amount;
        this.timestamp = timestamp;
        this.description = description;
    }
}