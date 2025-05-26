package com.example.geotourisme.adapter;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.geotourisme.R;
import com.example.geotourisme.model.Site;

import android.widget.ImageView;
import android.widget.ImageButton;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class SiteListAdapter extends ListAdapter<Site, SiteListAdapter.SiteViewHolder> {

    private OnItemClickListener listener;

    public SiteListAdapter() {
        super(DIFF_CALLBACK);
    }
    private List<Site> mSites ;

    private static final DiffUtil.ItemCallback<Site> DIFF_CALLBACK = new DiffUtil.ItemCallback<Site>() {
        @Override
        public boolean areItemsTheSame(@NonNull Site oldItem, @NonNull Site newItem) {
            return oldItem.getId_site() == newItem.getId_site();
        }

        @SuppressLint("DiffUtilEquals")
        @Override
        public boolean areContentsTheSame(@NonNull Site oldItem, @NonNull Site newItem) {
            return oldItem.equals(newItem);
        }
    };

    @NonNull
    @Override
    public SiteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.recyclerview_site_item, parent, false);
        return new SiteViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull SiteViewHolder holder, int position) {

        if (mSites != null) {
            Site currentSite = mSites.get(position);
            holder.nameTextView.setText(currentSite.getNom_site());
            holder.locationTextView.setText(currentSite.getLocalisation());
            holder.coordinatesTextView.setText("Lat: " + currentSite.getLatitude() + ", Long: " + currentSite.getLongitude());
            holder.reliefTextView.setText(currentSite.getNature_relief());

            String imagePath = currentSite.getImageUrl();
            if (imagePath != null && !imagePath.isEmpty()) {
                Glide.with(holder.itemView.getContext())
                        .load(imagePath)
                        .placeholder(R.drawable.ic_launcher_background)
                        .error(R.drawable.ic_launcher_foreground)
                        .into(holder.imageView);
            } else {
                holder.imageView.setImageResource(R.drawable.ic_launcher_foreground);
            }
        }
        else {
            holder.nameTextView.setText("No Site");
        }


    }


    public int getItemCount() {
        return (mSites != null) ? mSites.size() : 0;
    }
    public void setSites(List<Site> sites) {
        mSites = sites;
        notifyDataSetChanged();
    }


    public List<Site> getSites() {
        return mSites;
    }
    class SiteViewHolder extends RecyclerView.ViewHolder {
        private final ImageView imageView;
        private final TextView nameTextView;
        private final TextView locationTextView;
        private final TextView coordinatesTextView;
        private final TextView reliefTextView;
        private final ImageButton deleteButton;
        private final ImageButton editButton;

        public SiteViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.image_site);
            nameTextView = itemView.findViewById(R.id.name_site);
            locationTextView = itemView.findViewById(R.id.location_site);
            coordinatesTextView = itemView.findViewById(R.id.coordinates_site);
            reliefTextView = itemView.findViewById(R.id.relief_site);
            deleteButton = itemView.findViewById(R.id.button_delete);
            editButton = itemView.findViewById(R.id.button_edit);

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    Site clickedSite = getItem(position);


                    android.content.Intent intent = new android.content.Intent(v.getContext(), com.example.geotourisme.SiteDetailActivity.class);
                    intent.putExtra("site_id", clickedSite.getId_site());
                    v.getContext().startActivity(intent);
                }
            });
            editButton.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (listener != null && position != RecyclerView.NO_POSITION) {
                    listener.onEditClick(mSites.get(position));
                }
            });

            deleteButton.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (listener != null && position != RecyclerView.NO_POSITION) {
                    listener.onDeleteClick(mSites.get(position));
                }});
        }

    }

    public interface OnItemClickListener {
        void onEditClick(Site site);
        void onDeleteClick(Site site);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }
}
