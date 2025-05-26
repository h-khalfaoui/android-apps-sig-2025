package com.example.geotourisme.data.db;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.example.geotourisme.model.Commentaire;
import com.example.geotourisme.model.Review;
import com.example.geotourisme.model.Site;
import com.example.geotourisme.model.User;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Database(entities = {User.class, Site.class, Review.class, Commentaire.class}, version = 4, exportSchema = false)

public abstract class AppDatabase extends RoomDatabase {
    public abstract CommentaireDao commentaireDao();

    public abstract ReviewDao reviewDao();
    public abstract SiteDao siteDao();
    public abstract UserDao userDao();

    private static volatile AppDatabase INSTANCE;
    // Migration de la version 3 à 4

    static final Migration MIGRATION_5_6 = new Migration(4, 5) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            database.execSQL("CREATE TABLE IF NOT EXISTS reviews (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                    "userId INTEGER NOT NULL, " +
                    "siteId INTEGER NOT NULL, " +
                    "rating REAL NOT NULL, " +
                    "comment TEXT)");
        }
    };

    static final Migration MIGRATION_3_4 = new Migration(3, 4) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            // Ajout de la colonne 'image_url' dans la table 'sites'
            database.execSQL("ALTER TABLE sites ADD COLUMN image_url TEXT");
        }
    };

    // ExecutorService pour les opérations de la base de données en arrière-plan
    public static final ExecutorService databaseWriteExecutor =
            Executors.newFixedThreadPool(4); // Pool d



    public static AppDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                            AppDatabase.class, "geo_tourisme_db")
                    .allowMainThreadQueries()
                    .build();
        }
        return INSTANCE;
    }



}
