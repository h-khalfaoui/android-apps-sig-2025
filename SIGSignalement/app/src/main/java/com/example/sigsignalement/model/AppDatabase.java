package com.example.sigsignalement.model;

import androidx.room.Database;
import androidx.room.RoomDatabase;

@Database(entities = {User.class, Signalement.class}, version = 3, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {
    public abstract UserDao userDao();
    public abstract SignalementDao signalementDao();
}
