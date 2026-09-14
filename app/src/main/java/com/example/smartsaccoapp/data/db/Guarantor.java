package com.example.smartsaccoapp.data.db;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "guarantors")
public class Guarantor {
    @PrimaryKey(autoGenerate = true)
    public int id;
    public String memberName;
    public int loanId;
    public String status; // PENDING, APPROVED, REJECTED

    public Guarantor(String memberName, int loanId, String status) {
        this.memberName = memberName;
        this.loanId = loanId;
        this.status = status;
    }
}