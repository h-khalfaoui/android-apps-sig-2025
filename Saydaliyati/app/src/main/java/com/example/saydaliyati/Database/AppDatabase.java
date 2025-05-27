package com.example.saydaliyati.Database;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.example.saydaliyati.Models.Authority;
import com.example.saydaliyati.Models.GuardDate;
import com.example.saydaliyati.Models.Pharmacy;
import com.example.saydaliyati.Utils.SecurityUtils;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Database(entities = {Pharmacy.class, GuardDate.class, Authority.class},
        version = 3,
        exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {

    // Define DAOs
    public abstract PharmacyDAO pharmacyDAO();
    public abstract GuardDateDAO guardDateDAO();
    public abstract AuthorityDAO authorityDAO();

    // Singleton instance
    private static volatile AppDatabase INSTANCE;

    // Thread pool for database operations
    private static final int NUMBER_OF_THREADS = 4;
    public static final ExecutorService databaseWriteExecutor =
            Executors.newFixedThreadPool(NUMBER_OF_THREADS);

    // Migration from version 2 to 3
    static final Migration MIGRATION_2_3 = new Migration(2, 3) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            // Add new columns to pharmacies table
            database.execSQL("ALTER TABLE pharmacies ADD COLUMN phone TEXT");
            database.execSQL("ALTER TABLE pharmacies ADD COLUMN hours TEXT");
            database.execSQL("ALTER TABLE pharmacies ADD COLUMN hasParking INTEGER NOT NULL DEFAULT 0");

            // Add new columns to guard_dates table
            database.execSQL("ALTER TABLE guard_dates ADD COLUMN startTime TEXT");
            database.execSQL("ALTER TABLE guard_dates ADD COLUMN endTime TEXT");

            // Rename password to passwordHash in authorities table
            database.execSQL("ALTER TABLE Authority RENAME TO authorities_old");
            database.execSQL("CREATE TABLE authorities (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                    "username TEXT NOT NULL, " +
                    "passwordHash TEXT NOT NULL, " +
                    "role TEXT, " +
                    "lastLoginDate TEXT)");
            database.execSQL("INSERT INTO authorities (id, username, passwordHash) " +
                    "SELECT id, username, password FROM authorities_old");
            database.execSQL("DROP TABLE authorities_old");
        }
    };

    // Get database instance
    public static synchronized AppDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                            AppDatabase.class, "app_database")
                    .addMigrations(MIGRATION_2_3) // Add migration
                    .addCallback(new RoomDatabase.Callback() {
                        @Override
                        public void onCreate(@NonNull SupportSQLiteDatabase db) {
                            super.onCreate(db);
                            databaseWriteExecutor.execute(() -> {
                                // Create default admin user with hashed password
                                AuthorityDAO dao = INSTANCE.authorityDAO();
                                // Insert admin account using hash of "admin123"
                                String passwordHash = SecurityUtils.hashPassword("admin123");
                                Authority admin = new Authority("admin", passwordHash, "ADMIN");
                                dao.insert(admin);
                            });
                        }
                    })
                    .build();
        }
        return INSTANCE;
    }
}