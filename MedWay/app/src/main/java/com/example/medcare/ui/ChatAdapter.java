package com.example.medcare.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.medcare.R;
import com.example.medcare.model.ChatBot;

import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ChatViewHolder> {

    private List<String> questions;
    private ChatBot chatBot;
    private int expandedPosition = -1;

    public ChatAdapter(List<String> questions, ChatBot chatBot) {
        this.questions = questions;
        this.chatBot = chatBot;
    }

    @Override
    public ChatViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_chatbot, parent, false);
        return new ChatViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ChatViewHolder holder, int position) {
        String question = questions.get(position);
        holder.questionText.setText(question);

        boolean isExpanded = (expandedPosition == position);
        holder.responseText.setVisibility(isExpanded ? View.VISIBLE : View.GONE);
        holder.imageChatbot.setVisibility(isExpanded ? View.VISIBLE : View.GONE);

        if (isExpanded) {
            holder.responseText.setText(chatBot.getResponse(question));
            holder.responseText.startAnimation(AnimationUtils.loadAnimation(
                    holder.itemView.getContext(), R.anim.fade_in
            ));
        }

        holder.itemView.setOnClickListener(v -> {
            expandedPosition = isExpanded ? -1 : position;
            notifyDataSetChanged();
        });
    }

    @Override
    public int getItemCount() {
        return questions.size();
    }

    static class ChatViewHolder extends RecyclerView.ViewHolder {
        TextView questionText;
        TextView responseText;
        ImageView imageChatbot;

        ChatViewHolder(View itemView) {
            super(itemView);
            questionText = itemView.findViewById(R.id.textQuestion);
            responseText = itemView.findViewById(R.id.textResponse);
            imageChatbot = itemView.findViewById(R.id.imageChatbot);
        }
    }
}
