package com.example.smartsaccoapp.data.db;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface LoanDao {
    @Insert
    void insert(Loan loan);

    @Update
    void update(Loan loan);

    @Query("SELECT * FROM loans")
    LiveData<List<Loan>> getAllLoans();

    @Query("SELECT * FROM loans")
    List<Loan> getAllLoansSync();

    @Query("SELECT * FROM loans WHERE status = 'PENDING'")
    LiveData<List<Loan>> getPendingLoans();

    @Query("SELECT * FROM loans WHERE memberEmail = :email")
    LiveData<List<Loan>> getLoansByMember(String email);

    @Query("SELECT * FROM loans WHERE memberEmail = :email")
    List<Loan> getLoansByMemberSync(String email);

    @Query("SELECT COUNT(*) FROM loans")
    int getLoanCount();

    @Query("SELECT SUM(amount) FROM loans WHERE status = 'APPROVED'")
    double getTotalLoansAmount();
}
