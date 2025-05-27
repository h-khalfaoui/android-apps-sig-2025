package com.example.sigsignalement.model;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface SignalementDao {

    @Insert
    void insert(Signalement signalement);

    @Query("SELECT * FROM Signalement")
    List<Signalement> getAll();

    @Query("SELECT * FROM Signalement WHERE utilisateurEmail = :email")
    List<Signalement> getByUser(String email); // 🆕 si tu veux filtrer par utilisateur
}
