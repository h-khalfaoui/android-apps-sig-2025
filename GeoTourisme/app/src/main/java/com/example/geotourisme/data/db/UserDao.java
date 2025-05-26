package com.example.geotourisme.data.db;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.example.geotourisme.model.User;

import java.util.List;
@Dao
public interface UserDao {
   
        @Query("SELECT * FROM users WHERE LOWER(email) = LOWER(:email)")
        User getUserByEmail(String email);

    @Insert
    void insertUser(User user);
    @Query("SELECT * FROM users WHERE email = :email AND password = :password")
    User login(String email, String password);


    @Query("UPDATE users SET visites = visites + 1 WHERE id_user = :userId") // Utiliser 'id_user' ici
    void incrementVisites(int userId);

    @Query("SELECT visites FROM users WHERE id_user = :userId") // Utiliser 'id_user' ici aussi
    int getVisitesCount(int userId);
    @Query("SELECT nom FROM users WHERE id_user = :userId")
    String getUsernameById(int userId);



}
