package com.example.sigsignalement.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.sigsignalement.HistoriqueActivity;
import com.example.sigsignalement.SignalementActivity;
import com.example.sigsignalement.R;

public class HomeFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        Button buttonSignaler = view.findViewById(R.id.buttonSignaler);
        Button buttonHistorique = view.findViewById(R.id.buttonHistorique);

        buttonSignaler.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), SignalementActivity.class);
            startActivity(intent);
        });

        buttonHistorique.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), HistoriqueActivity.class);
            startActivity(intent);
        });

        return view;
    }
}
