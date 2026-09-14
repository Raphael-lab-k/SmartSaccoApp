package com.example.smartsaccoapp.data.db;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import java.util.List;

@Dao
public interface MarketItemDao {
    @Insert
    void insert(MarketItem item);

    @Update
    void update(MarketItem item);

    @Query("SELECT * FROM market_items WHERE status = 'AVAILABLE' ORDER BY id DESC")
    LiveData<List<MarketItem>> getAvailableItems();

    @Query("SELECT * FROM market_items WHERE id = :id")
    MarketItem getItemSync(int id);
}