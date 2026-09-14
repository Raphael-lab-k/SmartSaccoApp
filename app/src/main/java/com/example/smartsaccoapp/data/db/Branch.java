package com.example.smartsaccoapp.data.db;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "branches")
public class Branch {
    @PrimaryKey(autoGenerate = true)
    public int id;
    public String name;
    public String location;

    public Branch(String name, String location) {
        this.name = name;
        this.location = location;
    }
}