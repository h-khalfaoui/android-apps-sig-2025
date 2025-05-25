package com.example.quietspaceeee.data.db;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.example.quietspaceeee.data.model.Reservation;

import java.util.List;

@Dao
public interface ReservationDao {

    @Insert
    void insert(Reservation reservation);

    @Query("SELECT * FROM reservations")
    List<Reservation> getAllReservations();

    @Query("SELECT * FROM reservations WHERE userEmail = :email")
    List<Reservation> getReservationsByUserEmail(String email);


}
