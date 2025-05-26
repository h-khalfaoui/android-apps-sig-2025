package com.example.geotourisme.data.db;
import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;
import androidx.room.Delete;

import com.example.geotourisme.model.Site;

import java.util.List;

@Dao
public interface SiteDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insert(Site site);

    @Update
    void update(Site site);

    @Delete
    void delete(Site site);

//    @Query("DELETE FROM sites")
//    void deleteAllSites();

    //    @Query("SELECT * FROM sites")
//    List<Site> getAllSites();
//
//    @Query("SELECT * FROM sites WHERE id_site = :id")
//    Site getSiteById(int id);

    @Query("SELECT * FROM sites")
    List<Site> getAllSites();

    @Query("SELECT * FROM sites ORDER BY nom_site ASC")
    LiveData<List<Site>> getAllSitesOrderedByName();
    @Query("SELECT * FROM sites WHERE id_site = :siteId")
    Site getSiteById(int siteId);

    @Query("UPDATE sites SET visites = visites + 1 WHERE id_site = :siteId")
    void incrementVisites(int siteId);


    @Query("SELECT * FROM sites WHERE type_site = :type ORDER BY nom_site ASC")
    LiveData<List<Site>> getSitesByType(String type);

    @Query("SELECT visites FROM sites WHERE id_site = :siteId")
    int getVisitsForSite(int siteId);

    @Query("SELECT * FROM sites WHERE latitude BETWEEN :minLat AND :maxLat AND longitude BETWEEN :minLng AND :maxLng")
    LiveData<List<Site>> getSitesWithinBoundingBox(double minLat, double maxLat, double minLng, double maxLng);

    @Query("SELECT nom_site FROM sites WHERE nom_site LIKE '%' || :query || '%'")
    List<String> searchSiteNames(String query);
}
