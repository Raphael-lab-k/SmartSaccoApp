package com.example.smartsaccoapp.data.db;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "members")
public class Member {
    @PrimaryKey(autoGenerate = true)
    public int id;
    public String name;
    public String email;
    public String password;
    public String role; // MEMBER, ADMIN
    public String kycStatus; // PENDING, VERIFIED
    public String idNumber;
    public String address;
    public int branchId;
    public int trustScore; // 0 to 1000

    public Member() {} // Required for Firestore

    public Member(String name, String email, String password, String role, String kycStatus, String idNumber, String address, int branchId) {
        this.name = name;
        this.email = email;
        this.password = password;
        this.role = role;
        this.kycStatus = kycStatus;
        this.idNumber = idNumber;
        this.address = address;
        this.branchId = branchId;
        this.trustScore = 500; // Base score
    }
}