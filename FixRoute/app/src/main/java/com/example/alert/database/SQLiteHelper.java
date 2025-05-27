package com.example.alert.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class SQLiteHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "alert.db";
    private static final int DATABASE_VERSION = 3;
    private static final String USERS_TABLE = "users";
    private static final String CLAIMS_TABLE = "claims";

    public SQLiteHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS " + USERS_TABLE + " (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "email TEXT UNIQUE, " +
                "password TEXT, " +
                "role TEXT DEFAULT 'user')");

        db.execSQL("CREATE TABLE IF NOT EXISTS " + CLAIMS_TABLE + " (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "location TEXT NOT NULL, " +
                "description TEXT NOT NULL, " +
                "image BLOB, " +
                "status TEXT DEFAULT 'en attente')" // Nouveau champ pour le statut
        );

        db.execSQL("INSERT OR IGNORE INTO " + USERS_TABLE + " (email, password, role) VALUES ('admin@alert.com', 'admin', 'admin')");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 3) {
            db.execSQL("ALTER TABLE " + CLAIMS_TABLE + " ADD COLUMN status TEXT DEFAULT 'en attente'");
        }
    }

    public void updateClaimStatus(int claimId, String status) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("status", status);
        db.update(CLAIMS_TABLE, values, "id = ?", new String[]{String.valueOf(claimId)});
        db.close();
    }


    public Cursor getAllClaims() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT id, location, description, status FROM " + CLAIMS_TABLE, null);
    }


    public Cursor getClaimsByStatus(String status) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT id, location, description, status FROM " + CLAIMS_TABLE + " WHERE status = ?", new String[]{status});
    }
}
