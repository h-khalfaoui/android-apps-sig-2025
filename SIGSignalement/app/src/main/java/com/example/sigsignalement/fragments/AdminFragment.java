package com.example.sigsignalement.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sigsignalement.R;
import com.example.sigsignalement.model.AppDatabase;
import com.example.sigsignalement.model.AppDatabaseSingleton;
import com.example.sigsignalement.model.Signalement;
import com.example.sigsignalement.model.User;
import com.example.sigsignalement.adapter.UserAdapter;
import com.example.sigsignalement.adapter.SignalementAdapter;

import java.util.List;

public class AdminFragment extends Fragment {

    private RecyclerView recyclerViewUsers;
    private RecyclerView recyclerViewSignalements;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_admin, container, false);

        recyclerViewUsers = view.findViewById(R.id.recyclerViewUsers);
        recyclerViewSignalements = view.findViewById(R.id.recyclerViewSignalements);

        recyclerViewUsers.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerViewSignalements.setLayoutManager(new LinearLayoutManager(getContext()));

        AppDatabase db = AppDatabaseSingleton.getInstance(requireContext());
        List<User> users = db.userDao().getAllUsers();
        List<Signalement> signalements = db.signalementDao().getAll();

        recyclerViewUsers.setAdapter(new UserAdapter(users));
        recyclerViewSignalements.setAdapter(new SignalementAdapter(signalements));

        return view;
    }
}
