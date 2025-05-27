package com.example.saydaliyati.Utils;

import android.location.Location;

import com.example.saydaliyati.Models.Pharmacy;

import org.osmdroid.util.GeoPoint;
import org.osmdroid.util.BoundingBox;

import java.util.ArrayList;
import java.util.List;

/**
 * Utility class for location-related operations using OSMDroid
 */
public class LocationUtils {

    // Earth radius in kilometers
    private static final double EARTH_RADIUS = 6371.0;

    /**
     * Calculate distance between two locations in meters
     * @param startLatitude Starting latitude
     * @param startLongitude Starting longitude
     * @param endLatitude Ending latitude
     * @param endLongitude Ending longitude
     * @return Distance in meters
     */
    public static float calculateDistance(double startLatitude, double startLongitude,
                                          double endLatitude, double endLongitude) {
        float[] results = new float[1];
        Location.distanceBetween(startLatitude, startLongitude, endLatitude, endLongitude, results);
        return results[0]; // Distance in meters
    }

    /**
     * Calculate distance between a location and a pharmacy
     * @param location Current location
     * @param pharmacy Target pharmacy
     * @return Distance in meters
     */
    public static float calculateDistance(Location location, Pharmacy pharmacy) {
        if (location == null) return -1;

        return calculateDistance(
                location.getLatitude(), location.getLongitude(),
                pharmacy.getLatitude(), pharmacy.getLongitude()
        );
    }

    /**
     * Sort pharmacies by distance from current location
     * @param pharmacies List of pharmacies to sort
     * @param userLatitude User's latitude
     * @param userLongitude User's longitude
     */
    public static void sortPharmaciesByDistance(List<Pharmacy> pharmacies,
                                                double userLatitude, double userLongitude) {
        // Calculate distance for each pharmacy
        for (Pharmacy pharmacy : pharmacies) {
            float distance = calculateDistance(
                    userLatitude, userLongitude,
                    pharmacy.getLatitude(), pharmacy.getLongitude()
            );
            pharmacy.setDistance((double) distance);
        }

        // Sort list by distance
        pharmacies.sort((p1, p2) -> {
            if (p1.getDistance() == null) return 1;
            if (p2.getDistance() == null) return -1;
            return p1.getDistance().compareTo(p2.getDistance());
        });
    }

    /**
     * Calculate bounding box for nearby search
     * @param latitude Center latitude
     * @param longitude Center longitude
     * @param radiusInKm Radius in kilometers
     * @return Array with min/max lat/lon: [minLat, maxLat, minLon, maxLon]
     */
    public static double[] calculateBoundingBox(double latitude, double longitude, double radiusInKm) {
        // Converts degrees to radians
        double latRad = Math.toRadians(latitude);

        // Calculate lat/lon changes for given radius
        double latChange = Math.toDegrees(radiusInKm / EARTH_RADIUS);
        double lonChange = Math.toDegrees(radiusInKm / EARTH_RADIUS / Math.cos(latRad));

        // Return result as [minLat, maxLat, minLon, maxLon]
        return new double[] {
                latitude - latChange,
                latitude + latChange,
                longitude - lonChange,
                longitude + lonChange
        };
    }

    /**
     * Get BoundingBox that include all pharmacies and optionally the user location
     * @param pharmacies List of pharmacies
     * @param userLocation Optional user location (can be null)
     * @return BoundingBox object for OSMDroid
     */
    public static BoundingBox getBoundsForPharmacies(List<Pharmacy> pharmacies, GeoPoint userLocation) {
        if (pharmacies == null || pharmacies.isEmpty()) {
            // If no pharmacies, return bounds around user location or default
            if (userLocation == null) {
                // Default to some reasonable bounds if no data
                GeoPoint defaultCenter = new GeoPoint(35.7796, -5.8137); // Example: Tangier
                double offset = 0.01; // ~1km
                return new BoundingBox(
                        defaultCenter.getLatitude() + offset,
                        defaultCenter.getLongitude() + offset,
                        defaultCenter.getLatitude() - offset,
                        defaultCenter.getLongitude() - offset
                );
            } else {
                // Bounds around user location
                double offset = 0.01; // ~1km
                return new BoundingBox(
                        userLocation.getLatitude() + offset,
                        userLocation.getLongitude() + offset,
                        userLocation.getLatitude() - offset,
                        userLocation.getLongitude() - offset
                );
            }
        }

        // Builder for bounds
        double north = -90, east = -180, south = 90, west = 180;

        // Include all pharmacies
        for (Pharmacy pharmacy : pharmacies) {
            double lat = pharmacy.getLatitude();
            double lon = pharmacy.getLongitude();

            north = Math.max(north, lat);
            south = Math.min(south, lat);
            east = Math.max(east, lon);
            west = Math.min(west, lon);
        }

        // Include user location if available
        if (userLocation != null) {
            north = Math.max(north, userLocation.getLatitude());
            south = Math.min(south, userLocation.getLatitude());
            east = Math.max(east, userLocation.getLongitude());
            west = Math.min(west, userLocation.getLongitude());
        }

        // Add padding (about 10% of the size)
        double latPadding = (north - south) * 0.1;
        double lonPadding = (east - west) * 0.1;

        // Ensure minimum size
        if (latPadding < 0.005) latPadding = 0.005;
        if (lonPadding < 0.005) lonPadding = 0.005;

        return new BoundingBox(
                north + latPadding,
                east + lonPadding,
                south - latPadding,
                west - lonPadding
        );
    }

    /**
     * Filter pharmacies by proximity to a location
     * @param pharmacies List of all pharmacies
     * @param latitude Center latitude
     * @param longitude Center longitude
     * @param radiusInKm Radius in kilometers
     * @return Filtered list of pharmacies within the radius
     */
    public static List<Pharmacy> filterByProximity(List<Pharmacy> pharmacies,
                                                   double latitude, double longitude,
                                                   double radiusInKm) {
        List<Pharmacy> result = new ArrayList<>();

        for (Pharmacy pharmacy : pharmacies) {
            float distanceInMeters = calculateDistance(
                    latitude, longitude,
                    pharmacy.getLatitude(), pharmacy.getLongitude()
            );

            if (distanceInMeters <= radiusInKm * 1000) {
                pharmacy.setDistance((double) distanceInMeters);
                result.add(pharmacy);
            }
        }

        return result;
    }

    /**
     * Format distance for display
     * @param distanceInMeters Distance in meters
     * @return Formatted distance string (e.g., "250 m" or "2.5 km")
     */
    public static String formatDistance(double distanceInMeters) {
        if (distanceInMeters < 1000) {
            return String.format("%.0f m", distanceInMeters);
        } else {
            return String.format("%.1f km", distanceInMeters / 1000);
        }
    }
}