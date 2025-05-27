package com.example.sigsignalement.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.sigsignalement.LoginActivity;
import com.example.sigsignalement.R;
import com.example.sigsignalement.model.AppDatabase;
import com.example.sigsignalement.model.AppDatabaseSingleton;
import com.example.sigsignalement.model.User;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class ProfileFragment extends Fragment {

    private EditText editTextNom, editTextEmail;
    private Button buttonUpdate, buttonLogout, buttonPhoto;
    private ImageView imageProfile;

    private SharedPreferences prefs;
    private String currentEmail;
    private static final int PICK_IMAGE = 100;
    private byte[] imageData = null;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        editTextNom = view.findViewById(R.id.editTextNom);
        editTextEmail = view.findViewById(R.id.editTextEmail);
        buttonUpdate = view.findViewById(R.id.buttonUpdateProfile);
        buttonLogout = view.findViewById(R.id.buttonLogout);
        buttonPhoto = view.findViewById(R.id.buttonChangePhoto);
        imageProfile = view.findViewById(R.id.imageProfile);

        prefs = requireActivity().getSharedPreferences("USER_PREFS", Context.MODE_PRIVATE);
        currentEmail = prefs.getString("email", "");

        editTextNom.setText(prefs.getString("nom", ""));
        editTextEmail.setText(currentEmail);

        // 📷 Charger la photo depuis la base de données
        AppDatabase db = AppDatabaseSingleton.getInstance(requireContext());
        User user = db.userDao().getUserByEmail(currentEmail);
        if (user != null && user.getPhoto() != null) {
            Bitmap bitmap = BitmapFactory.decodeByteArray(user.getPhoto(), 0, user.getPhoto().length);
            imageProfile.setImageBitmap(bitmap);
        }

        buttonUpdate.setOnClickListener(v -> updateUserProfile());
        buttonLogout.setOnClickListener(v -> logout());
        buttonPhoto.setOnClickListener(v -> openGallery());

        return view;
    }

    private void updateUserProfile() {
        String newNom = editTextNom.getText().toString().trim();
        String newEmail = editTextEmail.getText().toString().trim();

        if (newNom.isEmpty() || newEmail.isEmpty()) {
            Toast.makeText(getContext(), "Champs vides", Toast.LENGTH_SHORT).show();
            return;
        }

        AppDatabase db = AppDatabaseSingleton.getInstance(requireContext());
        db.userDao().updateNom(currentEmail, newNom);

        if (!currentEmail.equals(newEmail)) {
            db.userDao().updateEmail(currentEmail, newEmail);
        }

        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("email", newEmail);
        editor.putString("nom", newNom);
        editor.apply();

        currentEmail = newEmail;

        Toast.makeText(getContext(), "Profil mis à jour ✅", Toast.LENGTH_SHORT).show();
    }

    private void logout() {
        prefs.edit().clear().apply();
        Intent intent = new Intent(getActivity(), LoginActivity.class);
        startActivity(intent);
        requireActivity().finish();
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, PICK_IMAGE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE && resultCode == Activity.RESULT_OK && data != null) {
            Uri selectedImage = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(requireActivity().getContentResolver(), selectedImage);
                imageProfile.setImageBitmap(bitmap);

                // Convertir en byte[]
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 90, stream);
                imageData = stream.toByteArray();

                // 🔥 Sauvegarder dans Room
                AppDatabase db = AppDatabaseSingleton.getInstance(requireContext());
                db.userDao().updatePhoto(currentEmail, imageData);

                Toast.makeText(getContext(), "Photo enregistrée ✅", Toast.LENGTH_SHORT).show();

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
