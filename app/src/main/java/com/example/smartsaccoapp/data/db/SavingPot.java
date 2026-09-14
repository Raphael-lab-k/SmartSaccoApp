package com.example.smartsaccoapp.data.db;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "saving_pots")
public class SavingPot {
    @PrimaryKey(autoGenerate = true)
    public int id;
    public String name;
    public double targetAmount;
    public double currentAmount;
    public String creatorEmail;
    public String status; // OPEN, LOCKED, ACHIEVED
    public String category; // e.g., Education, Land, Business

    public SavingPot(String name, double targetAmount, String creatorEmail, String category) {
        this.name = name;
        this.targetAmount = targetAmount;
        this.creatorEmail = creatorEmail;
        this.category = category;
        this.currentAmount = 0.0;
        this.status = "OPEN";
    }
    
    public SavingPot() {} // Firestore
}