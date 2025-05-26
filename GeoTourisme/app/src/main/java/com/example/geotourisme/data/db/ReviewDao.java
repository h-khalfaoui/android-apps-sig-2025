package com.example.geotourisme.data.db;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.example.geotourisme.model.Review;

@Dao
public interface ReviewDao {
    @Insert
    void insert(Review review);

    @Query("SELECT * FROM Review WHERE userId = :userId AND siteName = :siteName LIMIT 1")
    Review getUserReviewForSite(int userId, String siteName);
}
