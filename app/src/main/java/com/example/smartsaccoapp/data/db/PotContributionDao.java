package com.example.smartsaccoapp.data.db;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import java.util.List;

@Dao
public interface PotContributionDao {
    @Insert
    void insert(PotContribution contribution);

    @Query("SELECT * FROM pot_contributions WHERE potId = :potId ORDER BY timestamp DESC")
    LiveData<List<PotContribution>> getContributionsForPot(int potId);
}