package com.example.sigsignalement.model;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import java.util.List;
import com.example.sigsignalement.model.User;


@Dao
public interface UserDao {

    @Insert
    void insert(User user);

    @Query("SELECT * FROM User WHERE email = :email AND password = :password")
    User login(String email, String password);

    @Query("SELECT * FROM User WHERE email = :email")
    User findByEmail(String email);

    // ✅ Met à jour le nom d’un utilisateur à partir de son email
    @Query("UPDATE User SET nom = :nom WHERE email = :email")
    void updateNom(String email, String nom);

    // ✅ Met à jour l’email d’un utilisateur
    @Query("UPDATE User SET email = :newEmail WHERE email = :oldEmail")
    void updateEmail(String oldEmail, String newEmail);
    @Query("UPDATE User SET photo = :photo WHERE email = :email")
    void updatePhoto(String email, byte[] photo);

    @Query("SELECT * FROM User WHERE email = :email LIMIT 1")
    User getUserByEmail(String email);
    @Query("SELECT * FROM User WHERE isAdmin = 1 LIMIT 1")
    User getAdmin();

    @Query("SELECT * FROM User")
    List<User> getAllUsers();


}
