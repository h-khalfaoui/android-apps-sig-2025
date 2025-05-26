package com.example.geotourisme.ViewModel;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import com.example.geotourisme.data.db.AppDatabase;
import com.example.geotourisme.data.db.SiteDao;
import com.example.geotourisme.model.Site;

import org.osmdroid.util.GeoPoint;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;

public class SiteViewModel extends AndroidViewModel {

    private SiteDao siteDao;
    private LiveData<List<Site>> allSites;
    private MutableLiveData<String> currentTypeFilter = new MutableLiveData<>(null);
    private LiveData<List<Site>> filteredSites;
    private MutableLiveData<GeoPoint> bufferCenter = new MutableLiveData<>(null);
    private MutableLiveData<Double> bufferRadius = new MutableLiveData<>(null);

    public SiteViewModel(Application application) {
        super(application);
        AppDatabase db = AppDatabase.getInstance(application);
        siteDao = db.siteDao();
        allSites = siteDao.getAllSitesOrderedByName();



        filteredSites = Transformations.switchMap(currentTypeFilter, type -> {
            LiveData<List<Site>> typeFiltered = (type == null || type.isEmpty())
                    ? siteDao.getAllSitesOrderedByName()
                    : siteDao.getSitesByType(type);

            return Transformations.switchMap(typeFiltered, sites -> {
                MediatorLiveData<List<Site>> mediator = new MediatorLiveData<>();

                // Create combined update function
                Runnable updateFilter = () -> {
                    GeoPoint center = bufferCenter.getValue();
                    Double radius = bufferRadius.getValue();

                    if (center != null && radius != null && radius > 0) {
                        mediator.setValue(filterSitesWithinRadius(sites, center, radius));
                    } else {
                        mediator.setValue(sites);
                    }
                };

                // Add both sources
                mediator.addSource(bufferCenter, c -> updateFilter.run());
                mediator.addSource(bufferRadius, r -> updateFilter.run());

                return mediator;
            });
        });
    }

    private List<Site> filterSitesWithinRadius(List<Site> sites, GeoPoint center, double radiusKm) {
        List<Site> filtered = new ArrayList<>();
        for (Site site : sites) {
            double distance = calculateDistance(
                    center.getLatitude(),
                    center.getLongitude(),
                    site.getLatitude(),
                    site.getLongitude()
            );
            if (distance <= radiusKm) {
                filtered.add(site);
            }
        }
        return filtered;
    }

    private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        double earthRadius = 6371; // km
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat/2) * Math.sin(dLat/2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                        Math.sin(dLon/2) * Math.sin(dLon/2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
        return earthRadius * c;
    }

    // Méthode pour obtenir tous les sites
    public LiveData<List<Site>> getAllSites() {
        return allSites;
    }


    // Méthode pour insérer un site
    public void insert(Site site) {
        AppDatabase.databaseWriteExecutor.execute(() -> siteDao.insert(site));
    }

    // Méthode pour mettre à jour un site
    public void update(Site site) {
        AppDatabase.databaseWriteExecutor.execute(() -> siteDao.update(site));
    }
    public LiveData<List<Site>> getFilteredSites() {
        return filteredSites;
    }



    public void setTypeFilter(String type) {
        currentTypeFilter.setValue(type);
    }

    // Méthode pour supprimer un site
    public void delete(Site site) {
        AppDatabase.databaseWriteExecutor.execute(() -> siteDao.delete(site));
    }
    // Add this method to update buffer parameters
    public void setBufferParams(double lat, double lon, double radius) {
        if (radius > 0) {
            bufferCenter.postValue(new GeoPoint(lat, lon));
            bufferRadius.postValue(radius);
        } else {
            bufferCenter.postValue(null);
            bufferRadius.postValue(null);
        }
    }

    public LiveData<List<String>> searchSitesLive(String query) {
        MutableLiveData<List<String>> result = new MutableLiveData<>();
        if (query == null || query.trim().isEmpty()) {
            result.postValue(new ArrayList<>()); // Retourne une liste vide si query est vide
            return result;
        }

        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                List<String> suggestions = siteDao.searchSiteNames("%" + query + "%");
                result.postValue(suggestions != null ? suggestions : new ArrayList<>());
            } catch (Exception e) {
                result.postValue(new ArrayList<>()); // Retourne une liste vide en cas d'erreur
            }
        });
        return result;
}

}
