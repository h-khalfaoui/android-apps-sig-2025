package com.example.quietspaceeee.data.view;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.quietspaceeee.R;

import java.util.ArrayList;
import java.util.List;

import com.example.quietspaceeee.data.model.Cafe;

public class CafeAdapter extends BaseAdapter {

    private Context context;
    private ArrayList<Cafe> originalList;
    private ArrayList<Cafe> filteredList;

    public CafeAdapter(Context context, ArrayList<Cafe> cafes) {
        this.context = context;
        this.originalList = new ArrayList<>(cafes);
        this.filteredList = new ArrayList<>(cafes);
    }

    @Override
    public int getCount() {
        return filteredList.size();
    }

    @Override
    public Object getItem(int position) {
        return filteredList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return filteredList.get(position).getId();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        if (view == null) {
            view = LayoutInflater.from(context).inflate(R.layout.item_cafe, parent, false);
        }

        Cafe cafe = filteredList.get(position);

        TextView nameText = view.findViewById(R.id.cafe_name);
        TextView locationText = view.findViewById(R.id.cafe_description);
        ImageView imageView = view.findViewById(R.id.cafe_image);

        nameText.setText(cafe.getName());
        locationText.setText(cafe.getLocation());


        Glide.with(context)
                .load(cafe.getImageUrl())
                .placeholder(R.drawable.ic_cafe)
                .into(imageView);

        return view;
    }

    public void applyFilters(String query, String availability, List<String> selectedEquipments, String noise) {
        filteredList.clear();

        for (Cafe cafe : originalList) {

            String cafeName = cafe.getName() != null ? cafe.getName().toLowerCase() : "";
            String cafeLocation = cafe.getLocation() != null ? cafe.getLocation().toLowerCase() : "";
            String cafeEquipments = cafe.getEquipments() != null ? cafe.getEquipments().toLowerCase() : "";
            String cafeAvailability = cafe.getAvailability() != null ? cafe.getAvailability() : "";
            String cafeNoise = cafe.getNoiseLevel() != null ? cafe.getNoiseLevel() : "";

            boolean matchesEquipments = true;
            for (String equip : selectedEquipments) {
                if (!cafeEquipments.contains(equip.toLowerCase())) {
                    matchesEquipments = false;
                    break;
                }
            }

            boolean matchesQuery = cafeName.contains(query.toLowerCase()) || cafeLocation.contains(query.toLowerCase());
            boolean matchesAvailability = availability.equals("Tous") || cafeAvailability.equals(availability);
            boolean matchesNoise = noise.equals("Tous") || cafeNoise.equals(noise);

            if (matchesQuery && matchesAvailability && matchesEquipments && matchesNoise) {
                filteredList.add(cafe);
            }
        }


        notifyDataSetChanged();
    }

    public void setData(List<Cafe> newCafes) {
        this.originalList.clear();
        this.originalList.addAll(newCafes);

        this.filteredList.clear(); // Ajoute cette ligne
        this.filteredList.addAll(newCafes); // Et celle-ci

        notifyDataSetChanged();
    }



}