package com.example.saydaliyati.Adapters;

import android.content.Context;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.Filterable;

import com.example.saydaliyati.Database.AppDatabase;
import com.example.saydaliyati.Database.PharmacyDAO;
import com.example.saydaliyati.Models.Pharmacy;

import java.util.ArrayList;
import java.util.List;

public class SimplePlacesAdapter extends ArrayAdapter<String> implements Filterable {

    private List<TangierLocation> allLocations = new ArrayList<>();
    private List<String> allSuggestions = new ArrayList<>();
    private List<String> filteredSuggestions = new ArrayList<>();
    private List<Pharmacy> pharmacies = new ArrayList<>();
    private PharmacyDAO pharmacyDAO;
    private Context context;
    private boolean pharmaciesLoaded = false;

    public SimplePlacesAdapter(Context context) {
        super(context, android.R.layout.simple_dropdown_item_1line, new ArrayList<>());
        this.context = context;

        // Initialize the standard location data - this runs immediately
        initializeLocations();

        // Load pharmacies from database in background
        loadPharmacies();
    }

    private void initializeLocations() {
        // Clear any existing data
        allLocations.clear();
        allSuggestions.clear();

        // Original locations
        allLocations.add(new TangierLocation("Centre ville", 35.7796, -5.8137));
        allLocations.add(new TangierLocation("Médina", 35.7882, -5.8123));
        allLocations.add(new TangierLocation("Boukhalef", 35.7325, -5.9043));
        allLocations.add(new TangierLocation("Malabata", 35.7949, -5.7996));
        allLocations.add(new TangierLocation("Souani", 35.7694, -5.7962));
        allLocations.add(new TangierLocation("Dradeb", 35.7774, -5.8003));
        allLocations.add(new TangierLocation("Tétouan Plage", 35.7723, -5.8295));
        allLocations.add(new TangierLocation("Plateau", 35.7670, -5.8219));
        allLocations.add(new TangierLocation("Moujahidine", 35.7543, -5.8028));
        allLocations.add(new TangierLocation("Aéroport", 35.7264, -5.9161));

        // Additional locations in Tangier
        allLocations.add(new TangierLocation("Marjane", 35.7534, -5.7883));
        allLocations.add(new TangierLocation("Iberia", 35.7677, -5.8058));
        allLocations.add(new TangierLocation("Branes", 35.7589, -5.8114));
        allLocations.add(new TangierLocation("Casabarata", 35.7613, -5.8261));
        allLocations.add(new TangierLocation("Ziaten", 35.7360, -5.8967));
        allLocations.add(new TangierLocation("Mesnana", 35.7409, -5.8464));
        allLocations.add(new TangierLocation("Cap Spartel", 35.7897, -5.9344));
        allLocations.add(new TangierLocation("Rmilat", 35.7515, -5.8358));
        allLocations.add(new TangierLocation("Achakar", 35.7664, -5.9435));
        allLocations.add(new TangierLocation("Asilah", 35.4662, -6.0353)); // Nearby city

        // Major landmarks and points of interest
        allLocations.add(new TangierLocation("Gare de Tanger Ville", 35.7717, -5.8039)); // Train station
        allLocations.add(new TangierLocation("Port de Tanger", 35.7860, -5.8030));
        allLocations.add(new TangierLocation("Tanger City Mall", 35.7628, -5.8017));
        allLocations.add(new TangierLocation("Socco Alto", 35.7604, -5.8108));
        allLocations.add(new TangierLocation("Ibn Battouta Mall", 35.7547, -5.7937));
        allLocations.add(new TangierLocation("Hercules Caves", 35.7661, -5.9386));
        allLocations.add(new TangierLocation("Stade Ibn Batouta", 35.7378, -5.8762)); // Stadium
        allLocations.add(new TangierLocation("Marina Bay Tanger", 35.7884, -5.8099));
        allLocations.add(new TangierLocation("Café Hafa", 35.7927, -5.8153));
        allLocations.add(new TangierLocation("Musée de la Kasbah", 35.7901, -5.8136));

        // Educational institutions
        allLocations.add(new TangierLocation("Université Abdelmalek Essaâdi", 35.7636, -5.8548));
        allLocations.add(new TangierLocation("ENSA Tanger", 35.7636, -5.8537)); // Engineering school
        allLocations.add(new TangierLocation("ENCGT", 35.7636, -5.8518)); // Business school
        allLocations.add(new TangierLocation("Lycée Ibn Khaldoun", 35.7684, -5.8191));
        allLocations.add(new TangierLocation("American School of Tangier", 35.7731, -5.8271));

        // Hospitals and health centers
        allLocations.add(new TangierLocation("Hôpital Mohammed V", 35.7733, -5.8042));
        allLocations.add(new TangierLocation("Hôpital Italien", 35.7783, -5.8117));
        allLocations.add(new TangierLocation("Clinique Assaada", 35.7668, -5.7954));
        allLocations.add(new TangierLocation("Centre Hospitalier Mohammed VI", 35.7600, -5.7899));

        // Business districts and industrial areas
        allLocations.add(new TangierLocation("Zone Franche", 35.7207, -5.9087)); // Free zone
        allLocations.add(new TangierLocation("Tanger Automotive City", 35.6849, -5.8620));
        allLocations.add(new TangierLocation("Tétouan Shore", 35.7485, -5.8142));
        allLocations.add(new TangierLocation("Port Tanger Med", 35.8882, -5.5097)); // Major port

        // Beaches
        allLocations.add(new TangierLocation("Plage Municipale", 35.7889, -5.8094));
        allLocations.add(new TangierLocation("Plage Malabata", 35.7984, -5.7953));
        allLocations.add(new TangierLocation("Plage Achakar", 35.7668, -5.9407));
        allLocations.add(new TangierLocation("Plage Sidi Kacem", 35.7785, -5.9199));

        // Recent developments
        allLocations.add(new TangierLocation("Tanger Tech City", 35.7063, -5.7857));
        allLocations.add(new TangierLocation("Gare LGV", 35.7467, -5.8319)); // High-speed train station

        // Add all location names to suggestions
        for (TangierLocation location : allLocations) {
            allSuggestions.add("📍 " + location.name); // Add a location pin emoji to distinguish locations
        }

        // Initialize filtered suggestions
        filteredSuggestions.addAll(allSuggestions);
    }
    private void loadPharmacies() {
        // Load pharmacies from database in a background thread
        AppDatabase.databaseWriteExecutor.execute(() -> {
            try {
                pharmacyDAO = AppDatabase.getInstance(context).pharmacyDAO();
                pharmacies = pharmacyDAO.getAllPharmacies();

                // Create temporary list of pharmacy suggestions
                List<String> pharmacySuggestions = new ArrayList<>();
                for (Pharmacy pharmacy : pharmacies) {
                    pharmacySuggestions.add("🏥 " + pharmacy.getName());
                }

                // Update the suggestions list on the main thread
                if (context instanceof android.app.Activity) {
                    ((android.app.Activity) context).runOnUiThread(() -> {
                        // Add pharmacy names to suggestions list
                        allSuggestions.addAll(pharmacySuggestions);
                        filteredSuggestions.clear();
                        filteredSuggestions.addAll(allSuggestions);
                        pharmaciesLoaded = true;
                        notifyDataSetChanged();
                    });
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    public void getAutoComplete(String input) {
        if (input.isEmpty()) {
            // For empty input, show all suggestions
            filteredSuggestions.clear();
            filteredSuggestions.addAll(allSuggestions);
            notifyDataSetChanged();
            return;
        }

        // Otherwise apply the filter
        getFilter().filter(input);
    }

    @Override
    public int getCount() {
        return filteredSuggestions.size();
    }

    @Override
    public String getItem(int position) {
        if (position >= 0 && position < filteredSuggestions.size()) {
            return filteredSuggestions.get(position);
        }
        return "";
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                FilterResults results = new FilterResults();
                List<String> suggestions = new ArrayList<>();

                if (constraint == null || constraint.length() == 0) {
                    // No constraint, show all items
                    suggestions.addAll(allSuggestions);
                } else {
                    // Filter suggestions based on input
                    String filterPattern = constraint.toString().toLowerCase().trim();

                    for (String item : allSuggestions) {
                        if (item.toLowerCase().contains(filterPattern)) {
                            suggestions.add(item);
                        }
                    }
                }

                results.values = suggestions;
                results.count = suggestions.size();
                return results;
            }

            @Override
            @SuppressWarnings("unchecked")
            protected void publishResults(CharSequence constraint, FilterResults results) {
                filteredSuggestions.clear();
                if (results.values != null) {
                    filteredSuggestions.addAll((List<String>) results.values);
                }
                notifyDataSetChanged();
            }
        };
    }

    // Get location by its name
    public TangierLocation getLocationByName(String name) {
        // Check if it's a pharmacy (has the 🏥 prefix)
        if (name.startsWith("🏥 ")) {
            String pharmacyName = name.substring(3); // Remove the prefix

            // Find the pharmacy in our list
            for (Pharmacy pharmacy : pharmacies) {
                if (pharmacy.getName().equals(pharmacyName)) {
                    // Create a TangierLocation based on the pharmacy's coordinates
                    return new TangierLocation(pharmacyName, pharmacy.getLatitude(), pharmacy.getLongitude());
                }
            }
        }
        // Check if it's a location (has the 📍 prefix)
        else if (name.startsWith("📍 ")) {
            String locationName = name.substring(3); // Remove the prefix

            // Find the location in our list
            for (TangierLocation location : allLocations) {
                if (location.name.equals(locationName)) {
                    return location;
                }
            }
        }
        // If no prefix, try both lists
        else {
            // First check pharmacies
            for (Pharmacy pharmacy : pharmacies) {
                if (pharmacy.getName().equals(name)) {
                    return new TangierLocation(name, pharmacy.getLatitude(), pharmacy.getLongitude());
                }
            }

            // Then check locations
            for (TangierLocation location : allLocations) {
                if (location.name.equals(name)) {
                    return location;
                }
            }
        }

        return null;
    }
    // In SimplePlacesAdapter.java, add this method to manually refresh pharmacy data
    public void refreshPharmacies() {
        // Clear existing pharmacy data
        pharmacies.clear();

        // Load pharmacies from database in a background thread
        AppDatabase.databaseWriteExecutor.execute(() -> {
            try {
                pharmacyDAO = AppDatabase.getInstance(context).pharmacyDAO();
                List<Pharmacy> updatedPharmacies = pharmacyDAO.getAllPharmacies();

                // Create temporary list of pharmacy suggestions
                List<String> pharmacySuggestions = new ArrayList<>();
                for (Pharmacy pharmacy : updatedPharmacies) {
                    pharmacySuggestions.add("🏥 " + pharmacy.getName());
                }

                // Update the suggestions list on the main thread
                if (context instanceof android.app.Activity) {
                    ((android.app.Activity) context).runOnUiThread(() -> {
                        // Remove old pharmacy suggestions
                        List<String> locationSuggestions = new ArrayList<>();
                        for (String suggestion : allSuggestions) {
                            if (suggestion.startsWith("📍 ")) {
                                locationSuggestions.add(suggestion);
                            }
                        }

                        // Update the list with new pharmacies
                        allSuggestions.clear();
                        allSuggestions.addAll(locationSuggestions);
                        allSuggestions.addAll(pharmacySuggestions);

                        // Update the pharmacy list
                        pharmacies.clear();
                        pharmacies.addAll(updatedPharmacies);

                        // Update filtered suggestions
                        filteredSuggestions.clear();
                        filteredSuggestions.addAll(allSuggestions);

                        // Update the adapter
                        notifyDataSetChanged();
                    });
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
    // Location data class
    public static class TangierLocation {
        public String name;
        public double latitude;
        public double longitude;

        public TangierLocation(String name, double latitude, double longitude) {
            this.name = name;
            this.latitude = latitude;
            this.longitude = longitude;
        }
    }
}