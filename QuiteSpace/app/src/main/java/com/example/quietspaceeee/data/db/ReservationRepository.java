package com.example.quietspaceeee.data.db;

import android.content.Context;

import com.example.quietspaceeee.data.model.Reservation;

import java.util.List;

public class ReservationRepository {
    private ReservationDao reservationDao;

    public ReservationRepository(Context context) {
        AppDatabase db = AppDatabase.getInstance(context);
        reservationDao = db.reservationDao();
    }

    public void insert(Reservation reservation) {
        reservationDao.insert(reservation);
    }

    public List<Reservation> getAllReservations() {
        return reservationDao.getAllReservations();
    }

    public List<Reservation> getReservationsByUserEmail(String email) {
        return reservationDao.getReservationsByUserEmail(email);
    }

}
