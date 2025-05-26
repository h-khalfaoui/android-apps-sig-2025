package com.example.sigsignalement.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sigsignalement.R;
import com.example.sigsignalement.model.User;

import java.util.List;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder> {

    private final List<User> userList;

    public UserAdapter(List<User> userList) {
        this.userList = userList;
    }

    public static class UserViewHolder extends RecyclerView.ViewHolder {
        TextView textViewNom, textViewEmail;

        public UserViewHolder(View itemView) {
            super(itemView);
            textViewNom = itemView.findViewById(R.id.textNom);
            textViewEmail = itemView.findViewById(R.id.textEmail);
        }
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_user, parent, false);
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        User user = userList.get(position);
        holder.textViewNom.setText("Nom : " + user.getNom());
        holder.textViewEmail.setText("Email : " + user.getEmail());
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }
}
