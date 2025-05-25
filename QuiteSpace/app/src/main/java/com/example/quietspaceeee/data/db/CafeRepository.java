package com.example.quietspaceeee.data.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;

import com.example.quietspaceeee.data.model.Cafe;

public class CafeRepository {
    private CafeDatabaseHelper dbHelper;


    public CafeRepository(Context context) {
        dbHelper = new CafeDatabaseHelper(context);
    }

    public ArrayList<Cafe> getAllCafes() {
        ArrayList<Cafe> cafes = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM cafes", null);

        if (cursor.moveToFirst()) {
            do {
                cafes.add(cursorToCafe(cursor));
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return cafes;
    }

    public Cafe getCafeById(int id) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM cafes WHERE id = ?", new String[]{String.valueOf(id)});

        Cafe cafe = null;
        if (cursor.moveToFirst()) {
            cafe = cursorToCafe(cursor);
        }

        cursor.close();
        db.close();
        return cafe;
    }

    public long insertCafe(Cafe cafe) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("name", cafe.getName());
        values.put("city", cafe.getCity());
        values.put("location", cafe.getLocation());
        values.put("description", cafe.getDescription());
        values.put("type", cafe.getType());
        values.put("equipments", cafe.getEquipments());
        values.put("noiseLevel", cafe.getNoiseLevel());
        values.put("availability", cafe.getAvailability());
        values.put("averageCost", cafe.getAverageCost());
        values.put("imageUrl", cafe.getImageUrl());
        values.put("latitude", cafe.getLatitude());
        values.put("longitude", cafe.getLongitude());

        return db.insert("cafes", null, values); // retourne l'ID
    }

    private Cafe cursorToCafe(Cursor cursor) {
        return new Cafe(
                cursor.getInt(cursor.getColumnIndexOrThrow("id")),
                cursor.getString(cursor.getColumnIndexOrThrow("name")),
                cursor.getDouble(cursor.getColumnIndexOrThrow("latitude")),
                cursor.getDouble(cursor.getColumnIndexOrThrow("longitude")),
                cursor.getString(cursor.getColumnIndexOrThrow("description")),
                cursor.getString(cursor.getColumnIndexOrThrow("type")),
                cursor.getString(cursor.getColumnIndexOrThrow("availability")),
                cursor.getString(cursor.getColumnIndexOrThrow("equipments")),
                cursor.getString(cursor.getColumnIndexOrThrow("city")),
                cursor.getString(cursor.getColumnIndexOrThrow("location")),
                cursor.getString(cursor.getColumnIndexOrThrow("noiseLevel")),
                cursor.getDouble(cursor.getColumnIndexOrThrow("averageCost")),
                cursor.getString(cursor.getColumnIndexOrThrow("imageUrl"))
        );
    }


    public Cafe getCafeByNameAndLocation(String name, double latitude, double longitude) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query("cafes", null,
                "name=? AND latitude=? AND longitude=?",
                new String[]{name, String.valueOf(latitude), String.valueOf(longitude)},
                null, null, null);

        Cafe cafe = null;
        if (cursor.moveToFirst()) {
            cafe = new Cafe();
            cafe.setId(cursor.getInt(cursor.getColumnIndexOrThrow("id")));
            cafe.setName(cursor.getString(cursor.getColumnIndexOrThrow("name")));
            cafe.setLatitude(cursor.getDouble(cursor.getColumnIndexOrThrow("latitude")));
            cafe.setLongitude(cursor.getDouble(cursor.getColumnIndexOrThrow("longitude")));
            // Ajoute les autres champs si besoin
        }
        cursor.close();
        return cafe;
    }


    public void deleteCafeById(int id) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.delete("cafes", "id = ?", new String[]{String.valueOf(id)});
        db.close();
    }

    public boolean updateCafe(Cafe cafe) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put("name", cafe.getName());
        values.put("city", cafe.getCity());
        values.put("description", cafe.getDescription());
        values.put("location", cafe.getLocation());
        values.put("equipments", cafe.getEquipments());
        values.put("noiseLevel", cafe.getNoiseLevel());
        values.put("type", cafe.getType());
        values.put("availability", cafe.getAvailability());
        values.put("imageUrl", cafe.getImageUrl());

        int rowsAffected = db.update("cafes", values, "id = ?", new String[]{String.valueOf(cafe.getId())});
        db.close();
        return rowsAffected > 0;
    }
}