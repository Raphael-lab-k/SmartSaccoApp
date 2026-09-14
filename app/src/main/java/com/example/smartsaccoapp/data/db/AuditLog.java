package com.example.smartsaccoapp.data.db;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "audit_logs")
public class AuditLog {
    @PrimaryKey(autoGenerate = true)
    public int id;
    public long timestamp;
    public String actorEmail;
    public String action; // e.g., KYC_APPROVE, LOAN_APPROVE, DEPOSIT
    public String details;

    public AuditLog(long timestamp, String actorEmail, String action, String details) {
        this.timestamp = timestamp;
        this.actorEmail = actorEmail;
        this.action = action;
        this.details = details;
    }
}