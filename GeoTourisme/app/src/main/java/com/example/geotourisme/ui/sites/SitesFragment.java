package com.example.geotourisme.ui.sites;
import android.Manifest;

import static androidx.constraintlayout.motion.widget.Debug.getLocation;

import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

//  import com.example.geotourisme.Manifest;
import com.example.geotourisme.R;
import com.example.geotourisme.ViewModel.SiteViewModel;
import com.example.geotourisme.adapter.SiteListAdapter;
import com.example.geotourisme.databinding.FragmentHomeBinding;
import com.example.geotourisme.databinding.FragmentSitesBinding;
import com.example.geotourisme.model.Site;
import com.example.geotourisme.ui.home.HomeViewModel;

import java.util.List;

public class SitesFragment extends Fragment {
    private FragmentSitesBinding binding;
    private RecyclerView recyclerView;
    private SiteListAdapter adapter;
    private List<Site> siteList;
    private SiteViewModel siteViewModel;
    private ArrayAdapter<String> suggestionsAdapter;


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        SitesViewModel sitesViewModel =
                new ViewModelProvider(this).get(SitesViewModel.class);

        binding = FragmentSitesBinding.inflate(inflater, container, false);
        View root = binding.getRoot();



        suggestionsAdapter = new ArrayAdapter<>(
                getContext(),
                android.R.layout.simple_dropdown_item_1line
                );
        AutoCompleteTextView searchInput = root.findViewById(R.id.searchInput);
        searchInput.setAdapter(suggestionsAdapter);

        searchInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
//                String query = s.toString().trim();
//                if (!query.isEmpty()) {
//                    siteViewModel.(query).observe(getViewLifecycleOwner(), suggestions -> {
//                        if (suggestions != null && !suggestions.isEmpty()) {
//                            suggestionsAdapter.clear();
//                            suggestionsAdapter.addAlsearchSitesLivel(suggestions);
//                            searchInput.showDropDown();
//                        } else {
//                            searchInput.dismissDropDown();
//                        }
//                    });
//                } else {
//                    searchInput.dismissDropDown();
//                }
            }

            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void afterTextChanged(Editable s){}
});

        // Initialisation du RecyclerView
        RecyclerView recyclerView = root.findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        SiteListAdapter siteAdapter = new SiteListAdapter();
        recyclerView.setAdapter(siteAdapter);

        // Initialisation du ViewModel
        siteViewModel = new ViewModelProvider(this).get(SiteViewModel.class);
        siteViewModel.getAllSites().observe(getViewLifecycleOwner(), sites -> {
            siteAdapter.setSites(sites);
        });
        siteAdapter.setOnItemClickListener(new SiteListAdapter.OnItemClickListener() {
            @Override
            public void onDeleteClick(Site site) {
                siteViewModel.delete(site);
            }

            @Override
            public void onEditClick(Site site) {
                // Si tu veux gérer le bouton d’édition plus tard
            }
        });

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
