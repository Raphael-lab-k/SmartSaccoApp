package com.example.smartsaccoapp.data.db;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface AccountDao {
    @Insert
    void insert(Account account);

    @Update
    void update(Account account);

    @Query("SELECT * FROM accounts LIMIT 1")
    LiveData<Account> getAccount();

    @Query("SELECT * FROM accounts LIMIT 1")
    Account getAccountSync();
}