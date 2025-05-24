package com.example.medcare.ui;

import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.medcare.R;
import com.example.medcare.model.ChatBot;

import java.util.Arrays;
import java.util.List;

public class ChatBotFragment extends Fragment {

    private ChatBot chatBot;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chatbot, container, false);

        chatBot = new ChatBot();

        RecyclerView recyclerView = view.findViewById(R.id.recyclerViewChat);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        List<String> questions = Arrays.asList(
                "Comment ajouter un établissement ?",
                "Quels sont les établissements disponibles ?",
                "Comment voir la file d'attente ?",
                "Comment modifier mon profil ?",
                "Où voir les disponibilités ou horaires de travail ?",
                "Comment supprimer un établissement ?",
                "Quels sont les rôles des utilisateurs ?",
                "Quelles sont les mesures de sécurité ?",
                "Où puis-je trouver de l’aide ou du support ?",
                "Comment s’inscrire ou créer un compte ?",
                "Comment modifier ma spécialité ?",
                "Comment ajouter des horaires de disponibilité ?",
                "Comment consulter les détails d’un établissement ?",
                "Comment ajouter ou gérer un utilisateur ?"
        );

        ChatAdapter adapter = new ChatAdapter(questions, chatBot);
        recyclerView.setAdapter(adapter);

        return view;
    }
}
