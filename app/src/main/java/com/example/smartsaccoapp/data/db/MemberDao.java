package com.example.smartsaccoapp.data.db;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface MemberDao {
    @Insert
    void insert(Member member);

    @Update
    void update(Member member);

    @Query("SELECT * FROM members WHERE email = :email LIMIT 1")
    Member getMemberByEmail(String email);

    @Query("SELECT * FROM members")
    LiveData<List<Member>> getAllMembers();

    @Query("SELECT COUNT(*) FROM members")
    int getMemberCount();

    @Query("SELECT COUNT(*) FROM members WHERE kycStatus = 'PENDING'")
    int getPendingKycCount();

    @Query("SELECT * FROM members")
    List<Member> getMembersSync();
} // Force re-index
