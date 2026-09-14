package com.example.smartsaccoapp.data.db;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface BranchDao {
    @Insert
    void insert(Branch branch);

    @Query("SELECT * FROM branches")
    List<Branch> getAllBranches();
}