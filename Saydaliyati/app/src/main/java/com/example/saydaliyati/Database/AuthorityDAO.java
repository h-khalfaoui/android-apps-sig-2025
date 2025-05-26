package com.example.saydaliyati.Database;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.saydaliyati.Models.Authority;

import java.util.List;

@Dao
public interface AuthorityDAO {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(Authority authority);

    @Update
    void update(Authority authority);

    @Delete
    void delete(Authority authority);

    @Query("SELECT * FROM authorities WHERE username = :username LIMIT 1")
    Authority findByUsername(String username);

    @Query("SELECT * FROM authorities WHERE id = :id LIMIT 1")
    Authority findById(int id);

    @Query("SELECT * FROM authorities")
    List<Authority> getAllAuthorities();

    @Query("UPDATE authorities SET lastLoginDate = :loginDate WHERE id = :id")
    void updateLastLogin(int id, String loginDate);
}