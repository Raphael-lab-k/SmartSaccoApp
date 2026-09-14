package com.example.smartsaccoapp.data.db;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "vouches")
public class Vouch {
    @PrimaryKey(autoGenerate = true)
    public int id;
    public String voucherEmail;
    public String voucheeEmail;
    public long timestamp;

    public Vouch(String voucherEmail, String voucheeEmail, long timestamp) {
        this.voucherEmail = voucherEmail;
        this.voucheeEmail = voucheeEmail;
        this.timestamp = timestamp;
    }
    
    public Vouch() {}
}