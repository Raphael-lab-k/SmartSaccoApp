package com.example.smartsaccoapp.data.db;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface GuarantorDao {
    @Insert
    void insert(Guarantor guarantor);

    @Update
    void update(Guarantor guarantor);

    @Delete
    void delete(Guarantor guarantor);

    @Query("SELECT * FROM guarantors ORDER BY id DESC")
    LiveData<List<Guarantor>> getAllGuarantors();

    @Query("SELECT * FROM guarantors WHERE loanId = :loanId")
    LiveData<List<Guarantor>> getGuarantorsForLoan(int loanId);
}