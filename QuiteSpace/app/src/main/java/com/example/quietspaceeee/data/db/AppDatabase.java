package com.example.quietspaceeee.data.db;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.example.quietspaceeee.data.model.Reservation;

@Database(entities = {Reservation.class}, version = 2)
public abstract class AppDatabase extends RoomDatabase {
    private static AppDatabase instance;

    public abstract ReservationDao reservationDao();

    public static synchronized AppDatabase getInstance(Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(context.getApplicationContext(),
                            AppDatabase.class, "reservation_db")
                    .allowMainThreadQueries() // À éviter en production, préférable d'utiliser ViewModel + LiveData
                    .build();
        }
        return instance;
    }
}
