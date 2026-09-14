package com.example.smartsaccoapp.data.db;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "pot_contributions")
public class PotContribution {
    @PrimaryKey(autoGenerate = true)
    public int id;
    public int potId;
    public String memberEmail;
    public double amount;
    public long timestamp;

    public PotContribution(int potId, String memberEmail, double amount, long timestamp) {
        this.potId = potId;
        this.memberEmail = memberEmail;
        this.amount = amount;
        this.timestamp = timestamp;
    }
    
    public PotContribution() {} // Firestore
}