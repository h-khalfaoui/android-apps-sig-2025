package com.example.quietspaceeee.data.db;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.quietspaceeee.data.model.Cafe;

public class CafeDatabaseHelper extends SQLiteOpenHelper {

    public static final String DB_NAME = "quietspace.db";
    public static final int DB_VERSION = 5;  // Version incrémentée si tu modifies la structure
    private static CafeDatabaseHelper instance;

    public static synchronized CafeDatabaseHelper getInstance(Context context) {
        if (instance == null) {
            instance = new CafeDatabaseHelper(context.getApplicationContext());
        }
        return instance;
    }

    public CafeDatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        db.execSQL("CREATE TABLE cafes (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "name TEXT," +
                "latitude REAL," +
                "longitude REAL," +
                "description TEXT," +
                "type TEXT," +
                "availability TEXT," +
                "equipments TEXT," +
                "city TEXT," +
                "location TEXT," +
                "noiseLevel TEXT," +
                "averageCost REAL," +
                "imageUrl TEXT" +
                ")");

        db.execSQL("INSERT INTO cafes (name,latitude, longitude, description, type, availability, equipments, city, location, noiseLevel, averageCost, imageUrl) VALUES " +
                "('Café Zen',35.751720, -5.831950, 'Ambiance paisible, idéale pour les longues sessions de travail.', 'Café', 'Disponible', 'Wi-Fi, Snacks, Prises, Silence', 'Tanger', 'Rue des Fleurs',  'Silencieux', 20.0,  'https://images.unsplash.com/photo-1511920170033-f8396924c348')," +
                "('BiblioTech',35.751200, -5.832100, 'Bibliothèque moderne avec salles de travail équipées.', 'Bibliothèque', 'Partiellement disponible', 'Wi-Fi, Snacks, Éclairage LED, Calme', 'Tanger', 'Avenue des Universités', 'Très calme', 0.0, 'https://images.unsplash.com/photo-1511920170033-f8396924c348')," +
                "('WorkSpot', 35.751500, -5.832300,  'Espace de coworking avec tout le confort moderne.', 'Coworking', 'Disponible', 'Wi-Fi, Écran, Prises, Café gratuit', 'Tanger', 'Zone industrielle',  'Modéré', 35.0, 'https://images.unsplash.com/photo-1511920170033-f8396924c348')," +
                "('Le Café Étudiant',35.752000, -5.831800, 'Fréquenté par les étudiants, bon rapport qualité/prix.', 'Café', 'Disponible', 'Wi-Fi, Prises', 'Tanger', 'Centre-ville',  'Bruyant', 15.0, 'https://images.unsplash.com/photo-1511920170033-f8396924c348')," +
                "('Quiet Nest',35.751600, -5.831600, 'Endroit ultra calme, idéal pour la concentration.', 'Café', 'Disponible', 'Wi-Fi, Prises, Silence total', 'Tanger', 'Quartier administratif',  'Très calme', 22.0, 'https://images.unsplash.com/photo-1511920170033-f8396924c348')," +
                "('FocusLab',35.751500, -5.831900, 'Coworking moderne avec cabines insonorisées.', 'Coworking', 'Sur réservation', 'Wi-Fi, Cabines, Vidéo-projecteur', 'Tanger', 'Quartier techno',  'Silencieux', 40.0, 'https://images.unsplash.com/photo-1511920170033-f8396924c348');");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS cafes");
        onCreate(db);
    }
    public class CafeDao {
        private SQLiteDatabase db;

        public CafeDao(Context context) {
            CafeDatabaseHelper helper = new CafeDatabaseHelper(context);
            db = helper.getWritableDatabase();
        }

        public Cafe getCafeByNameAndLocation(String name, double lat, double lng) {
            String query = "SELECT * FROM cafes WHERE name = ? AND latitude = ? AND longitude = ?";
            Cursor cursor = db.rawQuery(query, new String[]{name, String.valueOf(lat), String.valueOf(lng)});
            if (cursor.moveToFirst()) {
                // Récupérer les infos de la base
            }
            cursor.close();
            return null;
        }

        // insert/update methods...
    }
}