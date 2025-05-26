package com.example.saydaliyati.Database;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.saydaliyati.Models.Pharmacy;

import java.util.List;

@Dao
public interface PharmacyDAO {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(Pharmacy pharmacy);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<Pharmacy> pharmacies);

    @Update
    void update(Pharmacy pharmacy);

    @Delete
    void delete(Pharmacy pharmacy);

    @Query("SELECT * FROM pharmacies WHERE id = :id")
    Pharmacy getPharmacyById(int id);

    @Query("SELECT * FROM pharmacies WHERE id IN (:pharmacyIds)")
    List<Pharmacy> getPharmaciesByIds(List<Integer> pharmacyIds);

    @Query("SELECT * FROM pharmacies")
    List<Pharmacy> getAllPharmacies();

    @Query("SELECT * FROM pharmacies")
    LiveData<List<Pharmacy>> getAllPharmaciesLiveData();

    @Query("SELECT * FROM pharmacies WHERE name LIKE '%' || :searchTerm || '%' OR address LIKE '%' || :searchTerm || '%'")
    List<Pharmacy> searchPharmacies(String searchTerm);
    // In your PharmacyDAO.java file, add this method:
    @Query("DELETE FROM pharmacies")
    int deleteAllPharmacies();
    @Query("SELECT * FROM pharmacies WHERE latitude BETWEEN :minLat AND :maxLat AND longitude BETWEEN :minLon AND :maxLon")
    List<Pharmacy> getNearbyPharmacies(double minLat, double maxLat, double minLon, double maxLon);
}