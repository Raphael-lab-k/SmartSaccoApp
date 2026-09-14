package com.example.smartsaccoapp.data.db;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface TransactionDao {
    @Insert
    void insert(Transaction transaction);

    @Query("SELECT * FROM transactions ORDER BY timestamp DESC")
    LiveData<List<Transaction>> getAllTransactions();

    @Query("SELECT * FROM transactions ORDER BY timestamp DESC LIMIT 5")
    LiveData<List<Transaction>> getLastFiveTransactions();

    @Query("SELECT SUM(amount) FROM transactions WHERE type = 'DEPOSIT'")
    double getTotalDeposits();

    @Query("SELECT SUM(amount) FROM transactions WHERE type = 'WITHDRAWAL'")
    double getTotalWithdrawals();
}