package com.example.smartsaccoapp.data.db;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import java.util.List;

@Dao
public interface SavingPotDao {
    @Insert
    long insert(SavingPot pot);

    @Update
    void update(SavingPot pot);

    @Query("SELECT * FROM saving_pots WHERE status != 'ACHIEVED'")
    LiveData<List<SavingPot>> getActivePots();

    @Query("SELECT * FROM saving_pots WHERE id = :id")
    SavingPot getPotSync(int id);

    @Query("SELECT * FROM saving_pots WHERE creatorEmail = :email")
    LiveData<List<SavingPot>> getPotsByMember(String email);
}