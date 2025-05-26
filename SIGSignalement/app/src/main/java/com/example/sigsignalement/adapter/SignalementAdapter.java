package com.example.sigsignalement.adapter;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.sigsignalement.R;
import com.example.sigsignalement.model.Signalement;

import java.util.List;


public class SignalementAdapter extends RecyclerView.Adapter<SignalementAdapter.ViewHolder> {

    private final List<Signalement> signalements;

    public SignalementAdapter(List<Signalement> signalements) {
        this.signalements = signalements;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView textViewInfo;
        public ImageView imageViewPhoto;

        public ViewHolder(View view) {
            super(view);
            textViewInfo = view.findViewById(R.id.textViewInfo);
            imageViewPhoto = view.findViewById(R.id.imageViewPhoto);
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_signalement, parent, false);
        return new ViewHolder(view);
    }


    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Signalement s = signalements.get(position);
        holder.textViewInfo.setText("Type: " + s.type + "\nLat: " + s.latitude + ", Lng: " + s.longitude);

        if (s.photo != null && s.photo.length > 0) {
            Bitmap bitmap = BitmapFactory.decodeByteArray(s.photo, 0, s.photo.length);
            holder.imageViewPhoto.setImageBitmap(bitmap);
        } else {
            holder.imageViewPhoto.setImageResource(R.drawable.ic_launcher_background); // ou une image par défaut
        }
    }


    @Override
    public int getItemCount() {
        return signalements.size();
    }
}
