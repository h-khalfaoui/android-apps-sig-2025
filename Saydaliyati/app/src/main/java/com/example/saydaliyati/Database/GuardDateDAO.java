package com.example.saydaliyati.Database;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.saydaliyati.Models.GuardDate;

import java.util.List;

@Dao
public interface GuardDateDAO {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(GuardDate guardDate);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<GuardDate> guardDates);

    @Update
    void update(GuardDate guardDate);

    @Delete
    void delete(GuardDate guardDate);

    @Query("DELETE FROM guard_dates WHERE pharmacyId = :pharmacyId AND guardDate = :date")
    void deleteGuardDateByPharmacyAndDate(int pharmacyId, String date);

    @Query("SELECT * FROM guard_dates WHERE pharmacyId = :pharmacyId")
    List<GuardDate> getGuardDatesByPharmacy(int pharmacyId);

    @Query("SELECT * FROM guard_dates WHERE pharmacyId = :pharmacyId")
    LiveData<List<GuardDate>> getGuardDatesByPharmacyLiveData(int pharmacyId);

    @Query("SELECT * FROM guard_dates")
    List<GuardDate> getAllGuardDates();

    @Query("SELECT * FROM guard_dates")
    LiveData<List<GuardDate>> getAllGuardDatesLiveData();

    @Query("SELECT * FROM guard_dates WHERE guardDate = :date")
    List<GuardDate> getByDate(String date);

    @Query("SELECT * FROM guard_dates WHERE guardDate BETWEEN :startDate AND :endDate")
    List<GuardDate> getByDateRange(String startDate, String endDate);

    @Query("SELECT COUNT(*) FROM guard_dates WHERE pharmacyId = :pharmacyId AND guardDate = :date")
    int isPharmacyOnDuty(int pharmacyId, String date);
}