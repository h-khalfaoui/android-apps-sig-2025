package com.example.sigsignalement.model;

import android.content.Context;

import androidx.room.Room;

public class AppDatabaseSingleton {
    private static AppDatabase instance;

    public static AppDatabase getInstance(Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(context.getApplicationContext(),
                            AppDatabase.class, "signalements_db")
                    .allowMainThreadQueries()
                    .build();
        }
        return instance;
    }
}
