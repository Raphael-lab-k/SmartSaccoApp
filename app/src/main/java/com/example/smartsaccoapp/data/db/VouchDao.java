package com.example.smartsaccoapp.data.db;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import java.util.List;

@Dao
public interface VouchDao {
    @Insert
    void insert(Vouch vouch);

    @Query("SELECT COUNT(*) FROM vouches WHERE voucheeEmail = :email")
    int getVouchCount(String email);
    
    @Query("SELECT * FROM vouches WHERE voucherEmail = :voucher AND voucheeEmail = :vouchee")
    Vouch getVouch(String voucher, String vouchee);

    @Query("SELECT * FROM vouches WHERE voucheeEmail = :email")
    LiveData<List<Vouch>> getVouchesForMember(String email);
}
